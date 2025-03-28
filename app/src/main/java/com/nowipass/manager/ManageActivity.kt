package com.nowipass.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.findViewTreeFullyDrawnReporterOwner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.nowipass.MainActivity
import com.nowipass.R
import com.nowipass.activitis
import com.nowipass.data_bases.pass_db
import com.nowipass.entropia
import com.nowipass.manager.recy.manage_adapter
import com.nowipass.manager.recy.manage_data
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.nowipass.bluetooth_things.Companion.dialog_bluetooth
import com.nowipass.bluetooth_things.Companion.search_bluetooth
import com.nowipass.data_bases.pass_db.Companion.elementos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

var tim = 0
var upgrade_items = false
var resume = false
var scrollTo = -1
lateinit var activity: Activity

class ManageActivity : AppCompatActivity() {
    private lateinit var spinner_dialog: AlertDialog.Builder
    @SuppressLint("NewApi", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activitis ++
        setContentView(R.layout.activity_manage)
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        supportActionBar?.setTitle("Manage")
        activity = this
        val add = findViewById<ConstraintLayout>(R.id.add_pass)
        val search_mode = findViewById<ConstraintLayout>(R.id.search_mode)
        val ali = intent.extras?.getString("alias").orEmpty()
        val filter = findViewById<SearchView>(R.id.filter)
        filter.visibility = View.INVISIBLE
        val recy = findViewById<RecyclerView>(R.id.recy)
        val adapter = manage_adapter(listOf(), filter)
        recy.adapter = adapter
        recy.layoutManager = LinearLayoutManager(this)

        val mk = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        var pref = EncryptedSharedPreferences.create(this, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        pref.edit().putString("ali", ali).apply()

        pref = EncryptedSharedPreferences.create(this, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        fun bye(texto:String){
            Toast.makeText(applicationContext, texto, Toast.LENGTH_SHORT).show()
            onPause()
        }

        if (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS){
            pref.edit().putBoolean("where", true).commit()
            comprobacion(this, resume)

        }else {
            bye("You need to activate a pin or biometric data to continue")
        }


        filter.queryHint = "position/search"

        filter.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            val regex = Regex("position/.+")
            override fun onQueryTextSubmit(text: String?): Boolean {
                if (text!!.matches(regex)){
                    val new = elementos.filter {dato -> dato.position.contains(text.split("/")[1].trim())}
                    adapter.upgrade(new)
                }else {
                    val new = elementos.filter {dato -> dato.asunto.contains(text.toString().trim())}
                    adapter.upgrade(new)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.toString().isEmpty()){
                    adapter.upgrade(elementos)
                }
                return true
            }
        })

        lifecycleScope.launch(Dispatchers.IO){
            while (true){
                if (upgrade_items) {
                    upgrade_items = false
                    withContext (Dispatchers.Main) {
                        if (scrollTo >= 0) {
                            adapter.upgrade_item(scrollTo)
                            scrollTo = -1
                        }else {
                            adapter.upgrade(elementos)
                        }
                    }

                }
                if (scrollTo > 0){
                    withContext (Dispatchers.Main){
                        recy.scrollToPosition(scrollTo)
                    }
                }
                delay(1000)
            }
        }


        fun secure_question(){
            val gen = gen(this)
            val dialog = Dialog(this)
            var ques_posi = 0
            val view = layoutInflater.inflate(R.layout.secure_question_interfaz, null)
            val spinner = view.findViewById<ConstraintLayout>(R.id.spinner_all)
            val spinner_question = view.findViewById<TextView>(R.id.spinner_questions)
            val questions = arrayOf("What are your two favorite numbers?", "What is your favorite day of the month?", "What is the name of your pet?", "What is the name of the loved one you care about the most?", "What is your favorite programming language?", "What is the password manager you feel most secure with?", "What is your favorite season of the year?", "What is your favorite month of the year?", "What city do you dream of living in?", "What is your birthday?", "What is your favorite food?", "What is your favorite company?")
            spinner_question.text = questions.toList().shuffled().take(1).joinToString("")

            spinner.setOnClickListener {
                spinner_dialog = AlertDialog.Builder(this)
                    .setTitle("Select your security question")
                    .setSingleChoiceItems (questions, ques_posi){_, position ->
                        ques_posi = position
                        spinner_question.text = questions[position]
                    }
                spinner_dialog.show()
            }

            val input = view.findViewById<AppCompatEditText>(R.id.input_answer)
            val fondo = view.findViewById<View>(R.id.fondo_opor)
            fondo.visibility = View.INVISIBLE
            val opor = view.findViewById<TextView>(R.id.opor)
            opor.visibility = View.INVISIBLE
            val go = view.findViewById<ShapeableImageView>(R.id.go)

            go.contentDescription = "This button lets you choose the answer to the question"

            go.setOnClickListener {
                if (input.text.toString().trim().isNotEmpty()) {
                    pref.edit().putBoolean("question_exist", true).apply()
                    pref = EncryptedSharedPreferences.create(this, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
                    pref.edit().putString("question", questions[ques_posi]).apply()
                    pref.edit().putString("aws", input.text.toString()).apply()

                    val kgen = KeyGenParameterSpec.Builder(input.text.toString(), KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()

                    val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                    kg.init(kgen)
                    kg.generateKey()

                    gen.sesion_register(1, mk)
                    dialog.dismiss()
                }else {
                    Toast.makeText(this, "you have to write something", Toast.LENGTH_SHORT).show()
                    input.setText("")
                }
            }
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }




        if (!pref.getBoolean("question_exist", false)){
            secure_question()
        }else {
            val db = pass_db(this)
            if (db.get()){
                pref = EncryptedSharedPreferences.create(applicationContext, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
                val pre = EncryptedSharedPreferences.create(this, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
                val repetidas = mutableMapOf<String, Int>()
                val rep_lista = mutableListOf<String>()
                fun recepcion(){
                    try {
                        val ks = KeyStore.getInstance("AndroidKeyStore")
                        ks.load(null)
                        for ((asunto, pass, position, iv) in elementos) {
                            val c = Cipher.getInstance("AES/GCM/NoPadding")
                            c.init(Cipher.DECRYPT_MODE, ks.getKey(ali, null), GCMParameterSpec(128, Base64.getDecoder().decode(iv)))
                            val password = String(c.doFinal(Base64.getDecoder().decode(pass)))
                            elementos[position.toInt()] = manage_data(String(Base64.getDecoder().decode(asunto)), password, position)
                            if (!pref.getBoolean("rep", false)) {
                                if (repetidas[password.trim()] == null) {
                                    repetidas.put(password.trim(), 1)
                                } else {
                                    repetidas[password.trim()] = repetidas[password.trim()]!! + 1
                                }
                            }
                        }
                        adapter.upgrade(elementos)

                        if (!pre.getBoolean("rep", false)) {
                            for ((pass, repe) in repetidas) {
                                if (repe > 1) {
                                    rep_lista.add(pass)
                                }
                            }
                            repetidas.clear()
                            if (rep_lista.isNotEmpty()) {
                                val alert_dia = AlertDialog.Builder(this)

                                alert_dia.setTitle("Passwords: ${rep_lista.joinToString(", ")}\n They are repeated")

                                rep_lista.clear()
                                alert_dia.setPositiveButton("Thanks") { _, _ -> }

                                alert_dia.setNegativeButton("Do not show again") { _, _ ->
                                    pre.edit().putBoolean("rep", true).commit()
                                }

                                alert_dia.show()


                            }

                        }

                        if (elementos.size > 1){
                            filter.visibility = View.VISIBLE
                        }
                    }catch (e: Throwable){
                        bye("Fatal error")
                        val manage = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("texto", e.toString())
                        manage.setPrimaryClip(clip)
                    }
                }

                if (!pref.getBoolean("aute", false)) {

                    val promt = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Authenticate yourself")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .build()

                    BiometricPrompt(this, ContextCompat.getMainExecutor(this), object: BiometricPrompt.AuthenticationCallback(){
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            pref.edit().putBoolean("aute", true).apply()
                            validity_duration(applicationContext, 120)
                            Thread.sleep(200)
                            recepcion()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            bye("I think something has gone wrong")
                        }
                    }).authenticate(promt)
                }else {
                    recepcion()
                }

            }
        }

        add.contentDescription = "Add passwords"

        add.setOnClickListener {
            val dialog = Dialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.add_edit_password_manage, null)

            val input_asunto = view.findViewById<EditText>(R.id.input_asunto)
            val input_password = view.findViewById<EditText>(R.id.input_password)
            val add_mac = view.findViewById<ConstraintLayout>(R.id.add_mac)
            add_mac.visibility = View.INVISIBLE
            val delet = view.findViewById<ConstraintLayout>(R.id.delete_pass)
            delet.visibility = View.INVISIBLE
            val edit = view.findViewById<ConstraintLayout>(R.id.edit_pass)
            edit.visibility = View.INVISIBLE
            val ad = view.findViewById<ShapeableImageView>(R.id.add_password)
            input_password.addTextChangedListener {valor ->
                val progress = view.findViewById<LinearProgressIndicator>(R.id.progress)
                entropia(valor.toString(), progress)
            }

            ad.contentDescription = "Add the password and subject"

            ad.setOnClickListener {
                pref = EncryptedSharedPreferences.create(this, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

                fun añadir(asunto:String, pass:String){
                    val db = pass_db(this)
                    val ks = KeyStore.getInstance("AndroidKeyStore")
                    ks.load(null)
                    val c = Cipher.getInstance("AES/GCM/NoPadding")
                    c.init(Cipher.ENCRYPT_MODE, ks.getKey(ali, null))

                    db.put(elementos.size, Base64.getEncoder().withoutPadding().encodeToString(asunto.toByteArray()), Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(pass.trim().toByteArray())), Base64.getEncoder().withoutPadding().encodeToString(c.iv))
                    elementos.add(manage_data(asunto, pass.trim(), elementos.size.toString()))
                    adapter.upgrade(elementos)
                    if (elementos.size > 1) {
                        filter.visibility = View.VISIBLE
                    }

                }
                if (!pref.getBoolean("aute", false)){
                    val promt = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Authenticate yourself")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .build()

                    BiometricPrompt(this, ContextCompat.getMainExecutor(this), object: BiometricPrompt.AuthenticationCallback(){
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            añadir(input_asunto.text.toString(), input_password.text.toString())
                            validity_duration(applicationContext, 120)
                            dialog.dismiss()
                            pref.edit().putBoolean("aute", true).apply()

                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            Toast.makeText(applicationContext, "It seems there was an error", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    }).authenticate(promt)
                }else {
                    añadir(input_asunto.text.toString(), input_password.text.toString())
                    dialog.dismiss()
                }
            }

            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        search_mode.setOnClickListener {
            Log.e("Boton", "seleccionado")
            val view = dialog_bluetooth()

            view.post {
                val infomration = view.findViewById<TextView>(R.id.information)
                 if (search_bluetooth(this)){
                    infomration.text = "Sharing keys..."
                }
            }

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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        activitis -= 1
        val mk = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pre = EncryptedSharedPreferences.create(this, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        pre.edit().putBoolean("where", false).apply()
        val pref = EncryptedSharedPreferences.create(this, "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        elementos.clear()
        if (pref.getBoolean("aute", false) && pre.getString("time_now", "") == "") {
            extraccion(this)
        }
    }
    override fun onPause() {
        super.onPause()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        resume = true
    }
}