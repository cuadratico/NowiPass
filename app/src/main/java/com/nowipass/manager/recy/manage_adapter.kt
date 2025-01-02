package com.nowipass.manager.recy

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nowipass.R

class manage_adapter(var list: List<manage_data>): RecyclerView.Adapter<manage_holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): manage_holder {
        return manage_holder(LayoutInflater.from(parent.context).inflate(R.layout.recy_manage_password, null))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: manage_holder, position: Int) {
        return holder.elemento(list[position])
    }


    fun upgrade(lista: List<manage_data>) {
        this.list = lista
        notifyDataSetChanged()
    }

}