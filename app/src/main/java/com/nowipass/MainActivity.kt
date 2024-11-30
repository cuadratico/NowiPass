package com.nowipass

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.nowipass.databinding.ActivityMainBinding
import com.nowipass.generator.adapter_switches
import com.nowipass.generator.switches_provier
import com.nowipass.generator.total
import java.text.Normalizer.Form

lateinit var password: TextView
fun generator(size: Int, password: TextView, progress: LinearProgressIndicator){
    var comprobador = false
    val provier = Provier_local()
    var pass = ""
    while(!comprobador){
        pass = ""
        var minus = 0
        var mayus = 0
        var numbers = 0
        var simb = 0

        for (i in 1..size){
            val ele = switches_provier.elements.split("").shuffled().take(1).joinToString ("")

            if (provier.minusculas.any { ele in it } && minus != 1){
                minus ++
            }else if (provier.mayusculas.any { ele in it } && mayus != 1){
                mayus ++
            }else if (provier.numeros.any { ele in it } && numbers != 1){
                numbers ++
            }else if (provier.simbolos.any {ele in it } && simb != 1){
                simb ++
            }
            pass += ele
        }

        if ((minus + mayus + numbers + simb) == total){
            comprobador = true
        }else {
            comprobador = false
        }
    }
    password.text = pass
    Formula.entropia(pass, progress)
}
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_qualifier, R.id.navigation_manager, R.id.navigation_generator
            )
        )



        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener {item ->
            if (item.itemId == R.id.navigation_qualifier){
                val dialog = Dialog(this)
                val view = LayoutInflater.from(this).inflate(R.layout.entropia_view, null)

                val edit = view.findViewById<EditText>(R.id.texto_editable)
                val p = view.findViewById<LinearProgressIndicator>(R.id.progress)
                edit.addTextChangedListener {dato ->
                    Formula.entropia(dato.toString(), p)
                }
                dialog.setContentView(view)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()

            }else if (item.itemId == R.id.navigation_generator){

                val dialog = BottomSheetDialog(this)
                val view = LayoutInflater.from(this).inflate(R.layout.generator_view, null)

                val progress= view.findViewById<LinearProgressIndicator>(R.id.progress)
                val recy = view.findViewById<RecyclerView>(R.id.selections)
                recy.adapter = adapter_switches(switches_provier.switches, progress)
                recy.layoutManager = LinearLayoutManager(this)
                val fondo_g = view.findViewById<View>(R.id.fondo_g)
                val d = this.getDrawable(R.drawable.bordes_blue1) as GradientDrawable
                d.setColor(Color.TRANSPARENT)
                fondo_g.background = d

                // elementos
                val reset = view.findViewById<ShapeableImageView>(R.id.reset)
                password = view.findViewById(R.id.password)
                val caracteres = view.findViewById<TextView>(R.id.caracteres)
                val size = view.findViewById<SeekBar>(R.id.size)
                val copy = view.findViewById<AppCompatButton>(R.id.copy_buttom)

                generator(4, password, progress)

                size.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        caracteres.text = "$p1 caracters"
                        generator(p1, password, progress)
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }

                })

                val animation = AnimationUtils.loadAnimation(this, R.anim.reset)
                animation.setAnimationListener(object: Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {
                        reset.isEnabled = false
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        reset.isEnabled = true
                    }

                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                })
                reset.setOnClickListener {
                    reset.startAnimation(animation)
                    generator(caracteres.text.toString().split(" ")[0].toInt(), password, progress)
                }

                copy.setOnClickListener {
                    val manage = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("texto", password.text.toString())
                    manage.setPrimaryClip(clip)
                    dialog.dismiss()
                }
                dialog.setContentView(view)
                dialog.show()

            }
            true
        }
    }
}