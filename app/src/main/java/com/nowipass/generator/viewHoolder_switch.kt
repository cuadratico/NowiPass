package com.nowipass.generator

import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.nowipass.Provier_local
import com.nowipass.R
import com.nowipass.generator
import com.nowipass.password

private val activate = mutableMapOf("A..Z" to false, "@,#,!..." to false, "0..9" to false)
var total = 1
class viewHoolder_switch(view: View, val pro: LinearProgressIndicator): RecyclerView.ViewHolder(view) {

    val provier = Provier_local()
    val indicator = view.findViewById<TextView>(R.id.indicator)
    val swit = view.findViewById<Switch>(R.id.swit)


    fun switches (switchData: switch_data){

        if (activate[switchData.indicator] == true){
            swit.isChecked = true
        }
        indicator.text = switchData.indicator
        swit.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                activate[switchData.indicator] =  true
                switches_provier.elements += switchData.list
                total ++
            }else {
                activate[indicator.text.toString()] = false
                total = 1
                switches_provier.elements = provier.minusculas.joinToString("")

                if (activate["A..Z"] == true){
                    switches_provier.elements += provier.mayusculas.joinToString("")
                    total ++
                }
                if (activate["@,#,!..."] == true){
                    switches_provier.elements += provier.simbolos.joinToString("")
                    total ++
                }
                if (activate["0..9"] == true){
                    switches_provier.elements += provier.numeros.joinToString("")
                    total ++
                }

            }
            generator(password.text.toString().toList().size, password, pro)
        }
    }
}