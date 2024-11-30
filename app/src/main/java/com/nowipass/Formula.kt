package com.nowipass

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.log2


class Formula {

    companion object {
        @SuppressLint("ResourceAsColor")
        fun entropia(password: String, progress: LinearProgressIndicator) {
            val provier = Provier_local()
            var mayus_c = 0
            var minus_c = 0
            var numbr_c = 0
            var symbols_c = 0

            for (valor in password) {
                if (provier.mayusculas.any { valor in it } && mayus_c != 26) {
                    mayus_c = 26
                } else if (provier.minusculas.any { valor in it } && minus_c != 26) {
                    minus_c = 26
                } else if (provier.numeros.any { valor in it } && numbr_c != 9) {
                    numbr_c = 10
                } else {
                    symbols_c++
                }
            }

            val final =
                (password.toList().size * log2((mayus_c + minus_c + numbr_c + symbols_c).toFloat())).toInt()

            if (final in 0..64) {
                progress.setIndicatorColor(ContextCompat.getColor(progress.context, R.color.red))
                progress.progress = final
            } else if (final in 65..112) {
                progress.setIndicatorColor(ContextCompat.getColor(progress.context, R.color.orange))
                progress.progress = final
            } else {
                if (final > 256) {
                    progress.progress = 256
                } else {
                    progress.progress = final
                }
                progress.setIndicatorColor(ContextCompat.getColor(progress.context, R.color.green))
            }
        }
    }
}
