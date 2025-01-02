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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        // Añadir un setOnLongClickLisener y un clickLisener
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

        all.setOnClickListener {

            fun actualizar(){
                upgrade_items = true
                Toast.makeText(position.context, "Pleas, wait 1 secons", Toast.LENGTH_SHORT).show()
                all.isEnabled = true
            }
            val db = pass_db(position.context)
            val dialog = Dialog(position.context)
            val view = LayoutInflater.from(position.context).inflate(R.layout.add_edit_password_manage, null)

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

            delete.setOnClickListener {
                val posi = position.text.toString().toInt()
                db.delete(posi + 1)
                elementos.removeAt(posi)
                all.isEnabled = false
                dialog.dismiss()
                actualizar()
            }

            edit.setOnClickListener {
                val mk = MasterKey.Builder(position.context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val pref = EncryptedSharedPreferences.create(position.context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

                val ks = KeyStore.getInstance("AndroidKeyStore")
                ks.load(null)

                val c = Cipher.getInstance("AES/GCM/NoPadding")
                c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("ali", ""), null))
                db.update(position.text.toString().toInt(), Base64.getEncoder().withoutPadding().encodeToString(input_a.text.toString().toByteArray()), Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(password.text.toString().toByteArray())), Base64.getEncoder().encodeToString(c.iv))
                elementos[position.text.toString().toInt()] = manage_data(input_a.text.toString(), input_p.text.toString(), position.text.toString())
                all.isEnabled = false
                dialog.dismiss()
                actualizar()
            }

            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

    }

}