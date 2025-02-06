package com.nowipass.data_bases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.nowipass.sesion.recy.sesion_data


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
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = ?", arrayOf("sesion_db"))
        db.close()
    }

    fun get():Boolean{
        val db = this.readableDatabase
        val consulta = db.rawQuery("SELECT * FROM sesion_db", null)

        fun recep(){
            sesiones.add(sesion_data(consulta.getString(1), consulta.getInt(2).toString(), consulta.getInt(0).toString(), consulta.getString(3)))
        }

        if (consulta.moveToFirst()){
            recep()
            while(consulta.moveToNext()){
                recep()
            }
            return true
        }else {
            return false
        }
    }

    companion object{
        val sesiones = mutableListOf<sesion_data>()
    }
}