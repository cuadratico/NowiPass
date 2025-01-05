package com.nowipass.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nowipass.MainActivity
import com.nowipass.activitis
import com.nowipass.data_bases.sesion_db
import com.nowipass.sesion.sesionActivity
import java.security.KeyStore
import java.security.MessageDigest
import java.time.LocalDateTime
import javax.crypto.KeyGenerator
import java.util.Base64
import javax.crypto.Cipher
import kotlin.random.Random

class gen(val context: Context) {

    fun finish(activity: Activity){
        val mk = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        pref.edit().putBoolean("inactive", true).apply()

        Toast.makeText(context, "You have no more attempts left", Toast.LENGTH_SHORT).show()
        activity.finishAffinity()

    }

    @SuppressLint("NewApi")
    fun gener(){
        val numero = Random.nextInt(0, 9)
        var password = ""
        for (valor in 0..9){
            val passGen = pass_gen()
            passGen.generador(9)

            val kp = KeyGenParameterSpec.Builder(passGen.password.split(passGen.password[5])[0], KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(120)
                .build()
            val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            kg.init(kp)
            kg.generateKey()
            if (valor == numero){
                password = passGen.password
            }
        }

        Toast.makeText(context, "Prepare to capture the screen", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "This is your password: $password", Toast.LENGTH_LONG).show()

        // eliminar para la version final
        val manage = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("texto", password)
        manage.setPrimaryClip(clip)

        val mk = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        var pref = EncryptedSharedPreferences.create(context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        pref.edit().putString("hash", Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA256").digest(password.split(password[4])[1].toByteArray()))).apply()

        pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        pref.edit().putBoolean("pass_exist", true).apply()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun recep(pass: AppCompatEditText, opor: TextView, activity: Activity, dialog: Dialog){


        val password = pass.text.toString()
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        val mk = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref  = EncryptedSharedPreferences.create(context, "ap", mk ,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        val hash = MessageDigest.getInstance("SHA256")

        if (ks.getKey(password.split(password[5])[0], null) != null || Base64.getEncoder().withoutPadding().encodeToString(hash.digest(password.split(password[4])[1].toByteArray())) == pref.getString("hash", "")){
            if (pref.getBoolean("question_exist", false)){
                sesion_register(1, mk)
            }
            val intent = Intent(context, ManageActivity::class.java)
                .putExtra("alias", password.split(password[5])[0])
            context.startActivity(intent)
            dialog.dismiss()
            activity.finish()
        }else {
            if (pref.getBoolean("question_exist", false)){
                sesion_register(0 ,mk)
            }
            if (opor.text.toString().toInt() <= 0){
                pref.edit().putString("opor", "0").apply()
                finish(activity)
            }else {
                opor.text = (opor.text.toString().toInt() - 1).toString()
                pref.edit().putString("opor", opor.text.toString()).apply()
                pass.setText("")
            }
        }

    }


    fun recep_answer (valor: EditText, opor: TextView, activity: Activity, dialog: Dialog){

        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        val mk = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        if (ks.getKey("Nowi", null) != null){
            val intent = Intent(context, sesionActivity::class.java)
                .putExtra("aws", valor.text.toString())

            context.startActivity(intent)
            activity.finish()
            dialog.dismiss()
        }else {
            Toast.makeText(context, "la clave no existe", Toast.LENGTH_LONG).show()
            if (opor.text.toString().toInt() > 0){
                opor.text = (opor.text.toString().toInt() - 1).toString()
                pref.edit().putString("opor", opor.text.toString()).apply()
                valor.setText("")
            }else {
                pref.edit().putString("opor", "0").apply()
                finish(activity)
            }
        }
    }

    @SuppressLint("NewApi")
    fun sesion_register(exito: Int, mk: MasterKey){
        val sesion = sesion_db(context)
        val pref = EncryptedSharedPreferences.create(context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("aws", ""), null))

        sesion.put(Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(LocalDateTime.now().toString().split("T").joinToString("-").toByteArray())), exito, Base64.getEncoder().withoutPadding().encodeToString(c.iv))
    }

}



