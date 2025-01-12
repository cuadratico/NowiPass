package com.nowipass.data_bases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class sesion_db(context: Context): SQLiteOpenHelper(context, "sesion_db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE sesion_db (id INTEGER PRIMARY KEY AUTOINCREMENT, time TEXT, succes INTEGER, iv TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun put(time: String, exi: Int, iv: String){
        val db = this.writableDatabase
        db.execSQL("INSERT INTO sesion_db (time, succes, iv) VALUES (?, ?, ?)", arrayOf(time, exi, iv))
    }

    fun delete(){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM sesion_db")
    }

    fun get(){
        val db = this.readableDatabase
        val consulta = db.rawQuery("SELECT * FROM sesion_db", null)

        if (consulta.moveToFirst()){
            autentificador_sesion = true
            time.add(consulta.getString(1))
            succes_list.add(consulta.getInt(2))
            vectores.add(consulta.getString(3))
            while(consulta.moveToNext()){
                time.add(consulta.getString(1))
                succes_list.add(consulta.getInt(2))
                vectores.add(consulta.getString(3))
            }
        }else {
            autentificador_sesion = false
        }
    }

    companion object{
        val time = mutableListOf<String>()
        val succes_list = mutableListOf<Int>()
        val vectores = mutableListOf<String>()
        var autentificador_sesion = false
    }
}