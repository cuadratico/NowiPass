package com.nowipass.sesion.recy

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nowipass.R

class sesion_holder(view: View): RecyclerView.ViewHolder(view) {

    val posicion = view.findViewById<TextView>(R.id.posicion)
    val succes = view.findViewById<TextView>(R.id.success)
    val sesion = view.findViewById<TextView>(R.id.sesion)

    fun elemento(sesionData: sesion_data){
        posicion.text = sesionData.position
        succes.text = sesionData.succes
        sesion.text = sesionData.time
    }
}