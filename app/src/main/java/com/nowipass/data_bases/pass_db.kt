package com.nowipass.data_bases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.nowipass.manager.recy.manage_data

class pass_db(context: Context): SQLiteOpenHelper(context, "pass_db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS pass_db (id INTEGER PRIMARY KEY, asunto TEXT, password TEXT, iv TEXT)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }


    fun drop(){
        val db = this.writableDatabase
        db.execSQL("DROP TABLE pass_db")
    }

    fun put(id: Int, asunto: String, password: String, iv: String){
        val db = this.writableDatabase
        db.execSQL("INSERT INTO pass_db (id, asunto, password, iv) VALUES (?, ?, ?, ?)", arrayOf(id, asunto, password, iv))
        db.close()
    }

    fun update(id: Int, asunto: String, password: String, iv:String){
        val db = this.writableDatabase
        db.execSQL("UPDATE pass_db SET asunto = ?, password = ?, iv = ? WHERE id = ?", arrayOf(asunto, password, iv, id))
        db.close()
    }

    fun delete(id: Int){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM pass_db WHERE id = ?", arrayOf(id))

        for (valores in 0..elementos.size - 1){
            db.execSQL("UPDATE pass_db SET id = ? WHERE id = ?", arrayOf(valores, elementos[valores].position.toInt()))
            elementos[valores].position = valores.toString()
            Log.e("lista", "posiciones actualizadas")
        }

        db.close()
    }

    fun get(): Boolean{
        val db = this.readableDatabase
        val consulta = db.rawQuery("SELECT * FROM pass_db", null)

        fun captacion(){
            elementos.add(manage_data(consulta.getString(1), consulta.getString(2), consulta.getInt(0).toString(), consulta.getString(3)))
        }
        if (consulta.moveToFirst()) {
            captacion()
            while (consulta.moveToNext()) {
                captacion()
            }
            return true
        }else {
            return false
        }
    }

    companion object{
        val elementos = mutableListOf<manage_data>()
    }
}