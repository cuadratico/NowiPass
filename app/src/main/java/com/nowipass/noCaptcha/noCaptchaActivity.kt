package com.nowipass.noCaptcha

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.nowipass.MainActivity
import com.nowipass.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.KeyGenerator
import kotlin.random.Random

class noCaptchaActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_no_captcha)

        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val mk = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref = EncryptedSharedPreferences.create(this, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val main = findViewById<ConstraintLayout>(R.id.main)
        val fondo = findViewById<ShapeableImageView>(R.id.fondo)
        val fondo_layout = findViewById<ConstraintLayout>(R.id.fondo_layout)
        val texto = findViewById<TextView>(R.id.texto)
        val imagen = findViewById<ShapeableImageView>(R.id.imagen)
        val box = findViewById<ShapeableImageView>(R.id.box)
        var toques= 0
        var tiempo= 0
        val scope = CoroutineScope(Dispatchers.Main)
        fun dialog(){
            val dialog = Dialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.no_captcha_block, null)
            val time = view.findViewById<TextView>(R.id.time)

            lifecycleScope.launch {
                for (i in 59.downTo(0)){
                    if (i == 0){
                        dialog.dismiss()
                        pref.edit().putBoolean("block", false).apply()
                        recreate()
                    }else {
                        delay(1000)
                        time.text = i.toString()
                    }
                }
            }
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
        fun actualizar(){
            val display = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(display)

            fondo_layout.x = Random.nextInt(0, (display.xdpi - 10).toInt()).toFloat()
            fondo_layout.y = Random.nextInt(500, display.heightPixels - 120).toFloat()

        }

        if (!pref.getBoolean("inactive", false)){
            finishAffinity()
        }else if (!pref.getBoolean("block", false)){
            actualizar()
            scope.launch{
                while(true){
                    tiempo ++
                    delay(1000)
                }
            }
        }else {
            dialog()
        }


        main.setOnClickListener {
            toques ++
            actualizar()
        }
        texto.setOnClickListener {
            toques ++
            actualizar()
        }
        imagen.setOnClickListener {
            toques ++
            actualizar()
        }
        fondo.setOnClickListener {
            toques ++
            actualizar()
        }

        box.setOnClickListener {
            scope.cancel()
            if (tiempo in 0..15  && toques < 7){
                box.setImageResource(R.drawable.check)
                Thread.sleep(100)
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }else{
                pref.edit().putBoolean("block", true).apply()
                dialog()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}