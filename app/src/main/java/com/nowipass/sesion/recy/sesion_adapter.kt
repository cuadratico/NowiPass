package com.nowipass.sesion.recy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nowipass.R

class sesion_adapter(var lista: List<sesion_data>): RecyclerView.Adapter<sesion_holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): sesion_holder {
        return sesion_holder(LayoutInflater.from(parent.context).inflate(R.layout.recy_manage_sesions, null))
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: sesion_holder, position: Int) {
        return holder.elemento(lista[position])
    }

    fun upgrade(){
        this.lista = sesiones
        notifyDataSetChanged()
    }
}