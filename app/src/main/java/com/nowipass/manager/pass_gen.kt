package com.nowipass.manager

import com.nowipass.Provier_local

class pass_gen {
    val provier = Provier_local()
    val lista = (provier.mayusculas.joinToString("") + provier.minusculas.joinToString("") + provier.simbolos.joinToString("") + provier.numeros.joinToString("")).toMutableList()
    var password = ""

    fun generador(size: Int){
        password = ""
        for (i in 1..size){
            val elemento = lista.shuffled().first()
            lista.removeAt(lista.indexOf(elemento))
            password += elemento
        }
    }
}