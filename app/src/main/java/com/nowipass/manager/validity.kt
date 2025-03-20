package com.nowipass.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.ContentInfoCompat.Flags
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nowipass.MainActivity
import com.nowipass.activitis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


private lateinit var scope_validity: Job
@RequiresApi(Build.VERSION_CODES.O)
fun validity_duration(context: Context, tiempo: Int){

    val mk = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    var pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    scope_validity = CoroutineScope(Dispatchers.IO).launch (start = CoroutineStart.LAZY){
        for(time in 1..tiempo){
            tim ++
            delay(1000)
        }

        if (pref.getBoolean("where", false)) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Your time is up", Toast.LENGTH_LONG).show()
        }
        pref = EncryptedSharedPreferences.create(context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        pref.edit().putBoolean("aute", false).apply()
        tim = 0
        resume = false
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val efect = VibrationEffect.createWaveform(longArrayOf(0, 50, 10, 50, 0), -1)
        vibrator.vibrate(efect)

        scope_validity.cancel()
    }
    scope_validity.start()

    Toast.makeText(context, "You have $tiempo seconds to do whatever you want.", Toast.LENGTH_LONG).show()
}
@RequiresApi(Build.VERSION_CODES.O)
fun extraccion(context: Context){
    val mk = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    pref.edit().putString("time_now", LocalDateTime.now().toString()).commit()
    pref.edit().putInt("validity_time", 120 - tim).commit()
}

@RequiresApi(Build.VERSION_CODES.O)
fun comprobacion(context: Context, inApp: Boolean = false){
    val mk = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    var pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    if (pref.getString("time_now", "") != "") {
        val time = pref.getString("time_now", "")?.split("T")!!
        val fecha = time[0].split("-")
        val fecha_actual = LocalDateTime.now().toString().split("T")[0].split("-")

        val tiempo = time[1].split(":")
        val tiempo_actual = LocalDateTime.now().toString().split("T")[1].split(":")

        val time_validity = pref.getInt("validity_time", 0)

        pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val operation = ((tiempo_actual[1].toInt() - 1) * 60 + tiempo_actual[2].split(".")[0].toInt()) - ((tiempo[1].toInt() - 1) * 60 + tiempo[2].split(".")[0].toInt())

        if ((fecha[0].toInt() - fecha_actual[0].toInt()) == 0 && (fecha[1].toInt() - fecha_actual[1].toInt()) == 0 && (fecha[2].toInt() - fecha_actual[2].toInt()) == 0 && (tiempo[0].toInt() - tiempo_actual[0].toInt()) == 0 && operation < time_validity){
            pref.edit().putBoolean("aute", true).commit()
            if (!inApp) {
                validity_duration(context, time_validity - operation)
            }else {
                Toast.makeText(context, "You have ${time_validity - operation}  seconds to do whatever you want.", Toast.LENGTH_LONG).show()
            }
        }else {
            pref.edit().putString("time_now", "").commit()
            pref.edit().putInt("validity_time", 0).commit()
            pref = EncryptedSharedPreferences.create(context, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
            pref.edit().putBoolean("aute", false).commit()
        }
    }
}