package com.nowipass.manager.recy

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.nowipass.R
import com.nowipass.data_bases.pass_db

class manage_adapter(var list: List<manage_data>, val fil: SearchView): RecyclerView.Adapter<manage_holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): manage_holder {
        return manage_holder(LayoutInflater.from(parent.context).inflate(R.layout.recy_manage_password, null))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: manage_holder, position: Int) {
        return holder.elemento(list[position])
    }


    fun upgrade(lista: List<manage_data>, upgra: Boolean = false) {
        if (elementos.size > 1){
            fil.visibility = View.VISIBLE
        }else {
            fil.visibility = View.INVISIBLE
        }
        if (upgra){
            val db = pass_db(fil.context)
            db.recalibracion()
        }
        this.list = lista
        notifyDataSetChanged()
    }

}