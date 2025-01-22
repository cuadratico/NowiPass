package com.nowipass.data_bases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.nowipass.manager.recy.elementos

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

    fun recalibracion(){
        val db = this.writableDatabase

        for (valores in 0..elementos.size - 1){
            db.execSQL("UPDATE pass_db SET id = ? WHERE id = ?", arrayOf(valores, elementos[valores].position))
            elementos[valores].position = valores.toString()
        }
        db.close()
    }

    fun delete(id: Int){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM pass_db WHERE id = ?", arrayOf(id))
        db.close()
    }

    fun get(){
        val db = this.readableDatabase
        val consulta = db.rawQuery("SELECT * FROM pass_db", null)

        if (consulta.moveToFirst()) {
            autentificador_pass = true
            asunto.add(consulta.getString(1))
            pass.add(consulta.getString(2))
            iv.add(consulta.getString(3))
            while (consulta.moveToNext()) {
                asunto.add(consulta.getString(1))
                pass.add(consulta.getString(2))
                iv.add(consulta.getString(3))
            }
        }else {
            autentificador_pass = false
        }
        db.close()
        Log.e("db", "cerrada")
    }

    companion object{
        val asunto = mutableListOf<String>()
        val pass = mutableListOf<String>()
        val iv = mutableListOf<String>()
        var autentificador_pass = false
    }
}