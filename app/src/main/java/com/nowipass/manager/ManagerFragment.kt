package com.nowipass.manager

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.renderscript.ScriptGroup.Input
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.nowipass.MainActivity
import com.nowipass.R
import com.nowipass.databinding.FragmentManagerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManagerFragment : Fragment() {

    private var _binding: FragmentManagerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentManagerBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val mk = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref_as = EncryptedSharedPreferences.create(requireContext(), "as", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        val pref_ap = EncryptedSharedPreferences.create(requireContext(), "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val boton = binding.manage
        val clock = binding.secureClock
        val padlock = binding.finish
        if (pref_as.getString("aws", "") == ""){
            clock.visibility = View.INVISIBLE
        }else {
            clock.visibility = View.VISIBLE
        }
        val gen = gen(requireContext())

        boton.setOnClickListener {
            val dialog = Dialog(requireContext())
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_password, null)

            val opor = view.findViewById<TextView>(R.id.opor)
            opor.text = pref_ap.getString("opor", "3")
            val visible = view.findViewById<ConstraintLayout>(R.id.visible)
            val v_image = view.findViewById<ShapeableImageView>(R.id.v_imagen)
            var v_valor = false
            val password = view.findViewById<AppCompatEditText>(R.id.input_pass)

            visible.setOnClickListener {
                if (v_valor){
                    v_valor = false
                    v_image.setImageResource(R.drawable.close_eye)
                    password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    password.transformationMethod = PasswordTransformationMethod.getInstance()
                }else {
                    v_valor = true
                    v_image.setImageResource(R.drawable.open_eye)
                    password.inputType = InputType.TYPE_CLASS_TEXT
                    password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                password.setSelection(password.text!!.length)
            }


            if (!pref_ap.getBoolean("pass_exist", false)){
                boton.contentDescription = "Wait for the password to be created"
                boton.isEnabled = false
                gen.gener()
                lifecycleScope.launch(Dispatchers.Main){
                    for (i in 0..7){
                        delay(1000)
                    }
                    boton.isEnabled = true
                    Toast.makeText(requireContext(), "You can continue now", Toast.LENGTH_SHORT).show()

                }
            }else {
                boton.contentDescription = "Write your password"
                dialog.setContentView(view)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }

            password.addTextChangedListener {dato ->
                if (dato!!.toList().size == 9 && dato.toString().trim().isNotEmpty()){
                    gen.recep(password, opor, requireActivity(), dialog)
                }

                if (dato.toList().size > 9){
                    Toast.makeText(requireContext(), "You shouldn't put texts that big.", Toast.LENGTH_SHORT).show()
                    password.setText("")
                }

            }


        }

        clock.contentDescription = "Check your account login logs"

        clock.setOnClickListener {
            val dialog = Dialog(requireContext())
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.secure_question_interfaz, null)


            val spinner_all = view.findViewById<ConstraintLayout>(R.id.spinner_all)
            spinner_all.visibility = View.INVISIBLE
            val opor = view.findViewById<TextView>(R.id.opor)
            opor.text = pref_ap.getString("opor", "3")
            val question = view.findViewById<TextView>(R.id.question)
            question.text = pref_as.getString("question", "")
            val input_answer = view.findViewById<AppCompatEditText>(R.id.input_answer)
            val go = view.findViewById<ShapeableImageView>(R.id.go)

            go.setOnClickListener {
                gen.recep_answer(input_answer, opor, requireActivity(), dialog)
            }
            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        padlock.contentDescription = "Delete all information from NowiPass"

        padlock.setOnClickListener {
            val alertD = AlertDialog.Builder(requireContext())
                .setTitle("By continuing you will delete all your NowiPass information")
                .setPositiveButton("continue") { _, _ ->
                    finish(requireContext(), requireActivity(), true)
                }
                .setNegativeButton("cancel"){_, _ ->}
            alertD.show()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}