package com.nowipass

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.biometric.BiometricPrompt
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nowipass.data_bases.pass_db
import com.nowipass.data_bases.pass_db.Companion.elementos
import com.nowipass.manager.activity
import com.nowipass.manager.recy.manage_data
import com.nowipass.manager.scrollTo
import com.nowipass.manager.upgrade_items
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher

class bluetooth_things {
    companion object {
        lateinit var socket: BluetoothSocket
        val adaptador_bluetooth = BluetoothAdapter.getDefaultAdapter()
        const val uuid = "00000000-0000-1000-8000-00805F9B34FB"
        val asime_list = mutableListOf<String>()
        lateinit var dialog_bluetooth: Dialog
        lateinit var pass_share: String
        lateinit var corutina_bluetooth: Job

        @RequiresApi(Build.VERSION_CODES.O)
        fun coru_blue(context: Context) {
            corutina_bluetooth = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {

                    while (true) {
                        if (socket.inputStream.available() > 0) {
                            val buffer = ByteArray(socket.inputStream.available())
                            socket.inputStream.read(buffer)

                            if (buffer.size >= 292) {
                                val public_user = KeyFactory.getInstance("RSA")
                                    .generatePublic(X509EncodedKeySpec(buffer))

                                val c = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                                c.init(Cipher.ENCRYPT_MODE, public_user)

                                socket.outputStream.write(c.doFinal(pass_share.toByteArray()))

                                pass_share = ""
                            } else {
                                val private = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(asime_list[1])))

                                var c = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                                c.init(Cipher.DECRYPT_MODE, private)
                                val pass_rec = c.doFinal(buffer)
                                val db = pass_db(context)
                                val mk = MasterKey.Builder(context)
                                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                    .build()
                                val pref = EncryptedSharedPreferences.create(context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
                                val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

                                c = Cipher.getInstance("AES/GCM/NoPadding")

                                fun add_pass() {
                                    c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("ali", ""), null))
                                    db.put(elementos.size, "No subject", Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(pass_rec)), Base64.getEncoder().withoutPadding().encodeToString(c.iv))
                                    elementos.add(manage_data("No subject", String(pass_rec), elementos.size.toString()))
                                    upgrade_items = true
                                    scrollTo = elementos.size
                                }
                                if (!pref.getBoolean("aute", false)){
                                    val promt = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Autentificate")
                                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                                        .build()

                                    withContext (Dispatchers.Main) {
                                        BiometricPrompt(context as FragmentActivity, ContextCompat.getMainExecutor(context), object : BiometricPrompt.AuthenticationCallback() {
                                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                    super.onAuthenticationSucceeded(result)
                                                    add_pass()
                                                }
                                        }).authenticate(promt)
                                    }
                                }else {
                                    add_pass()
                                }
                            }
                            break
                        }
                        delay(500)
                    }
                dialog_bluetooth.dismiss()
                corutina_bluetooth.cancel()
                asime_list.clear()
            }
            corutina_bluetooth.start()
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun asi(){
            val kg = KeyPairGenerator.getInstance("RSA")
            kg.initialize(2048)
            val cla = kg.generateKeyPair()

            val claves = cla

            asime_list.add(Base64.getEncoder().withoutPadding().encodeToString(claves.public.encoded))
            asime_list.add(Base64.getEncoder().withoutPadding().encodeToString(claves.private.encoded))
            Log.e("lista", asime_list.toString())
            socket.outputStream.write(claves.public.encoded)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun search_bluetooth(context: Context): Boolean{

            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    socket = adaptador_bluetooth.listenUsingRfcommWithServiceRecord("chat", UUID.fromString(uuid)).accept(10000)
                    asi()
                    Toast.makeText(context, "The connection has been established", Toast.LENGTH_SHORT).show()
                    coru_blue(context)
                    return true
                }
            }catch (e: Exception){
                Log.e("error", e.toString())
                Toast.makeText(context, "No device has been connected", Toast.LENGTH_SHORT).show()
                dialog_bluetooth.dismiss()
            }
            return false
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun conexion_bluetooth(context: Context, mac: String): Boolean{
            Log.e("Se esta ejecutando la funcion", "true")
            try {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val device = adaptador_bluetooth.getRemoteDevice(mac)
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
                    socket.connect()
                    Toast.makeText(context, "The connection has been established", Toast.LENGTH_SHORT).show()
                    coru_blue(context)
                    return true
                }

            }catch (e: Exception){
                Log.e("error", e.toString())
                Toast.makeText(context, "You have not connected to any device", Toast.LENGTH_SHORT).show()
                dialog_bluetooth.dismiss()
            }
            return false
        }

        fun dialog_bluetooth(): View{

            Log.e("Dialog", "mostrado")
            dialog_bluetooth = Dialog(activity)
            val view_dialog_blue = LayoutInflater.from(activity).inflate(R.layout.conexino_to_device, null)

            dialog_bluetooth.setContentView(view_dialog_blue)
            dialog_bluetooth.setCancelable(false)
            dialog_bluetooth.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog_bluetooth.show()

            return view_dialog_blue

        }

        fun very_mac(mac: String): Boolean{

            val regex = Regex("^([0-9A-F]{2}){5}([0-9A-F]{2})")

            if (regex.matches(mac.split(":").joinToString(""))){
                return true
            }
            return false
        }
    }
}