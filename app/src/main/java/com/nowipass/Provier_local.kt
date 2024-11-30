package com.nowipass

class Provier_local {

    val mayusculas = ('A'..'Z').map {it.toString() }
    val minusculas = ('a'..'z').map { it.toString() }
    val numeros = (0..9).map { it.toString() }
    val simbolos = listOf("!", "@", "#", "$", "%", "&", "*","-", "_", "=", "+")
}