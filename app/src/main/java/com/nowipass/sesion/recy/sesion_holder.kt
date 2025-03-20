package com.nowipass.sesion.recy

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nowipass.R

class sesion_holder(view: View): RecyclerView.ViewHolder(view) {

    val posicion = view.findViewById<TextView>(R.id.posicion)
    val succes = view.findViewById<TextView>(R.id.success)
    val sesion = view.findViewById<TextView>(R.id.sesion)
    val click = view.findViewById<View>(R.id.click)

    fun elemento(sesionData: sesion_data){
        posicion.text = sesionData.position
        succes.text = sesionData.succes
        sesion.text = sesionData.time

        val context = sesion.context

        click.setOnClickListener {
            val dialog = Dialog(context)
            val dialog_view = LayoutInflater.from(context).inflate(R.layout.see_the_password, null)

            val succes_view = dialog_view.findViewById<TextView>(R.id.asus)
            val sesion_view = dialog_view.findViewById<TextView>(R.id.pass)

            succes_view.text = succes.text.toString()
            sesion_view.text = sesion.text.toString()

            dialog.setContentView(dialog_view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

    }
}