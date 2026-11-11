package com.example.act2_ud1

import android.content.Context
import androidx.core.content.edit

// AQUI SE GUARDAN LOS AJUSTES (EN OTRO ARCHIVO DISTINTO AL DE LAS LINEAS)
object Ajustes {

    private const val ARCHIVO = "ajustes"
    private const val CLAVE_ORDEN = "orden"

    // ORDEN POR CODIGO O POR RECORRIDO
    const val ORDEN_CODIGO = "codigo"
    const val ORDEN_RECORRIDO = "recorrido"

    // LEER EL ORDEN (SI NO HAY NADA, POR CODIGO)
    fun leerOrden(contexto: Context): String {
        val prefs = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        return prefs.getString(CLAVE_ORDEN, ORDEN_CODIGO) ?: ORDEN_CODIGO
    }

    // GUARDAR EL ORDEN
    fun guardarOrden(contexto: Context, orden: String) {
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit {
            putString(CLAVE_ORDEN, orden)
        }
    }
}