package com.nowipass.manager.recy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.nowipass.R
import com.nowipass.data_bases.pass_db
import com.nowipass.manager.upgrade_items
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.nowipass.bluetooth_things.Companion.conexion_bluetooth
import com.nowipass.bluetooth_things.Companion.dialog_bluetooth
import com.nowipass.bluetooth_things.Companion.pass_share
import com.nowipass.bluetooth_things.Companion.very_mac
import com.nowipass.entropia
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.nowipass.data_bases.pass_db.Companion.elementos
import com.nowipass.manager.activity
import com.nowipass.manager.scrollTo

@SuppressLint("NewApi")
class manage_holder(view: View): RecyclerView.ViewHolder(view) {

    val all = view.findViewById<ConstraintLayout>(R.id.all)
    val asunto = view.findViewById<TextView>(R.id.asunto)
    val password = view.findViewById<TextView>(R.id.password)
    val position = view.findViewById<TextView>(R.id.posicion)

    fun elemento(data: manage_data){
        asunto.text = data.asunto
        password.text = data.password
        position.text = data.position

        all.setOnLongClickListener {
            val dialog = Dialog(position.context)
            val view = LayoutInflater.from(position.context).inflate(R.layout.see_the_password, null)

            val asus = view.findViewById<TextView>(R.id.asus)
            val pass = view.findViewById<TextView>(R.id.pass)

            asus.text = asunto.text.toString()
            pass.text = password.text.toString()

            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            true
        }

        all.contentDescription = "Check your password number ${position.text}"

        all.setOnClickListener {
            val posi = position.text.toString().toInt()
            fun actualizar(){
                all.isEnabled = false
                upgrade_items = true
                Toast.makeText(position.context, "Wait", Toast.LENGTH_SHORT).show()
                all.isEnabled = true
            }

            val db = pass_db(position.context)
            val dialog = Dialog(position.context)
            val view = LayoutInflater.from(position.context).inflate(R.layout.add_edit_password_manage, null)

            val add_mac = view.findViewById<ConstraintLayout>(R.id.add_mac)
            val resumen = view.findViewById<TextView>(R.id.resumen)
            resumen.text = "Edit your password"
            val input_a = view.findViewById<EditText>(R.id.input_asunto)
            input_a.hint = "Edit your subject"
            input_a.setText(asunto.text.toString())
            val input_p = view.findViewById<EditText>(R.id.input_password)
            input_p.hint = "Edit your password"
            input_p.setText(password.text.toString())
            val delete = view.findViewById<ConstraintLayout>(R.id.delete_pass)
            val add = view.findViewById<ShapeableImageView>(R.id.add_password)
            add.visibility = View.INVISIBLE
            val edit = view.findViewById<ConstraintLayout>(R.id.edit_pass)
            val progreso = view.findViewById<LinearProgressIndicator>(R.id.progress)
            entropia(input_p.text.toString(), progreso)
            input_p.addTextChangedListener {dato ->
                entropia(dato.toString(), progreso)
            }

            delete.contentDescription = "Delete your password"

            delete.setOnClickListener {

                elementos.removeAt(posi)
                db.delete(posi)
                dialog.dismiss()
                actualizar()
            }

            edit.contentDescription = "Edit your password"

            val mk = MasterKey.Builder(position.context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val pref = EncryptedSharedPreferences.create(position.context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

            edit.setOnClickListener {

                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)

                val c = Cipher.getInstance("AES/GCM/NoPadding")
                c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("ali", ""), null))
                db.update(position.text.toString().toInt(), Base64.getEncoder().withoutPadding().encodeToString(input_a.text.toString().toByteArray()), Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(input_p.text.toString().toByteArray())), Base64.getEncoder().encodeToString(c.iv))
                elementos[position.text.toString().toInt()] = manage_data(input_a.text.toString(), input_p.text.toString(), position.text.toString())
                dialog.dismiss()
                actualizar()
                scrollTo = posi
            }

            add_mac.setOnClickListener {
                pass_share = input_p.text.toString()
                dialog.dismiss()
                val dialog_mac = Dialog(asunto.context)
                val view_mac = LayoutInflater.from(asunto.context).inflate(R.layout.add_mac, null)

                val add_button = view_mac.findViewById<ConstraintLayout>(R.id.add_device)
                val input_mac = view_mac.findViewById<EditText>(R.id.input_mac)


                add_button.setOnClickListener {
                    if (very_mac(input_mac.text.toString())){
                        dialog_mac.dismiss()
                        val view = dialog_bluetooth()

                        view.post {
                            val information = view.findViewById<TextView>(R.id.information)
                            if (conexion_bluetooth(asunto.context, input_mac.text.toString())){
                                information.text = "Sharing keys..."
                            }
                        }
                    }else {
                        Toast.makeText(asunto.context, "The mac address is not correct", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog_mac.setContentView(view_mac)
                dialog_mac.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog_mac.show()
            }
            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

    }

}