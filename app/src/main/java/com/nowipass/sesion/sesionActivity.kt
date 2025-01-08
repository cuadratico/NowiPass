package com.nowipass.sesion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.nowipass.MainActivity
import com.nowipass.R
import com.nowipass.activitis
import com.nowipass.data_bases.sesion_db
import com.nowipass.data_bases.sesion_db.Companion.autentificador_sesion
import com.nowipass.data_bases.sesion_db.Companion.succes_list
import com.nowipass.data_bases.sesion_db.Companion.time
import com.nowipass.data_bases.sesion_db.Companion.vectores
import com.nowipass.manager.extraccion
import com.nowipass.sesion.recy.sesion_adapter
import com.nowipass.sesion.recy.sesion_data
import com.nowipass.sesion.recy.sesiones
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class sesionActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sesion)
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        activitis ++

        val recy = findViewById<RecyclerView>(R.id.recy)
        val delet = findViewById<ConstraintLayout>(R.id.delete)
        val info = findViewById<TextView>(R.id.info)
        info.visibility = View.INVISIBLE
        val adapter = sesion_adapter(sesiones)
        val ali = intent.extras?.getString("aws").orEmpty()
        val sesiones_db = sesion_db(this)


        recy.adapter = adapter
        recy.layoutManager = LinearLayoutManager(this)


        sesiones_db.get()
        if (autentificador_sesion){
            val exitos_list = listOf("An unsuccessful attempt", "A successful attempt")


            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)

            for (position in 0..time.size - 1){
                val c = Cipher.getInstance("AES/GCM/NoPadding")
                c.init(Cipher.DECRYPT_MODE, ks.getKey(ali, null), GCMParameterSpec(128, Base64.getDecoder().decode(vectores[position])))

                sesiones.add(sesion_data(String(c.doFinal(Base64.getDecoder().decode(time[position]))), exitos_list[succes_list[position]], position.toString()))
            }
            adapter.upgrade(sesiones)
            time.clear()
            succes_list.clear()
            vectores.clear()
            autentificador_sesion = false

        }else {
            onPause()
            Toast.makeText(this, "No session records", Toast.LENGTH_SHORT).show()
        }

        delet.setOnClickListener {
            sesiones_db.delete()
            sesiones.clear()
            adapter.upgrade(sesiones)
            delet.visibility = View.INVISIBLE
            info.visibility = View.VISIBLE
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @SuppressLint("NewApi")
    override fun onDestroy() {
        super.onDestroy()

        val mk = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref = EncryptedSharedPreferences.create(this, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        sesiones.clear()
        activitis -= 1

        if (activitis == 0 && pref.getBoolean("aute", false)){
            extraccion(this)
        }
    }
}