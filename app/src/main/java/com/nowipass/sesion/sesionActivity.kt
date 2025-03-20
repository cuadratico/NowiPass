package com.nowipass.sesion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
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
import com.nowipass.data_bases.sesion_db.Companion.sesiones
import com.nowipass.sesion.recy.sesion_adapter
import com.nowipass.sesion.recy.sesion_data
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

val exitos_list = listOf("An unsuccessful attempt", "A successful attempt")
class sesionActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sesion)
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        supportActionBar?.setTitle("Sesion")
        activitis ++

        val recy = findViewById<RecyclerView>(R.id.recy)
        val delet = findViewById<ConstraintLayout>(R.id.delete)
        val fill = findViewById<SearchView>(R.id.fill)
        val info = findViewById<TextView>(R.id.info)
        info.visibility = View.INVISIBLE
        val adapter = sesion_adapter(sesiones)
        val ali = intent.extras?.getString("aws").orEmpty()
        val sesiones_db = sesion_db(this)
        recy.adapter = adapter
        recy.layoutManager = LinearLayoutManager(this)


        if (sesiones_db.get()){

            if (sesiones.size < 2){
                fill.visibility = View.INVISIBLE
            }
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)

            for ((time, succest, position, iv) in sesiones){
                val c = Cipher.getInstance("AES/GCM/NoPadding")
                c.init(Cipher.DECRYPT_MODE, ks.getKey(ali, null), GCMParameterSpec(128, Base64.getDecoder().decode(iv)))
                Log.e("posicion", position)
                sesiones[position.toInt() - 1] = sesion_data(String(c.doFinal(Base64.getDecoder().decode(time))), exitos_list[succest.toInt()], position)
            }
            adapter.upgrade(sesiones)

        }else {
            delet.visibility = View.INVISIBLE
            info.visibility = View.VISIBLE
            fill.visibility = View.INVISIBLE
        }

        fill.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {

                val fill_list = sesiones.filter { dato -> dato.time.contains(text.toString())  }
                adapter.upgrade(fill_list)
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if (text.toString().isEmpty()){
                    adapter.upgrade(sesiones)
                }
                return true
            }

        })
        delet.contentDescription = "Delete all your session logs"

        delet.setOnClickListener {
            sesiones_db.delete()
            sesiones.clear()
            adapter.upgrade(sesiones)
            delet.visibility = View.INVISIBLE
            info.visibility = View.VISIBLE
            fill.visibility = View.INVISIBLE
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
        sesiones.clear()
        activitis -= 1
    }
}