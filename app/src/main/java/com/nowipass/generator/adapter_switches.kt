package com.nowipass.generator

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.nowipass.R

class adapter_switches(val lista: List<switch_data>, val pro: LinearProgressIndicator): RecyclerView.Adapter<viewHoolder_switch>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHoolder_switch {
        return viewHoolder_switch(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_switches, null), pro)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: viewHoolder_switch, position: Int) {
        return holder.switches(lista[position])
    }

}