package com.nowipass.generator

import com.nowipass.Provier_local

class switches_provier {

    companion object {
        val provier = Provier_local()
        val switches = listOf(
            switch_data("@,#,!...", provier.simbolos.joinToString("")),
            switch_data("A..Z", provier.mayusculas.joinToString("")),
            switch_data("0..9", provier.numeros.joinToString(""))
        )

        var elements = provier.minusculas.joinToString("")
    }


}