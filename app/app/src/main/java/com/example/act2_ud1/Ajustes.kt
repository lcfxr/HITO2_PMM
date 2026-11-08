package com.example.act2_ud1

import android.content.Context
import androidx.core.content.edit

// AJUSTES: GUARDA LAS PREFERENCIAS DEL USUARIO (SEPARADAS DE LOS DATOS DE LAS LÍNEAS)
object Ajustes {

    private const val ARCHIVO = "ajustes"     // ARCHIVO DISTINTO AL DE LAS LÍNEAS
    private const val CLAVE_ORDEN = "orden"   // ETIQUETA DE LA PREFERENCIA

    // LOS DOS VALORES POSIBLES DEL ORDEN
    const val ORDEN_CODIGO = "codigo"
    const val ORDEN_RECORRIDO = "recorrido"

    // LEER EL ORDEN ELEGIDO (SI NUNCA SE HA ELEGIDO, POR CÓDIGO)
    fun leerOrden(contexto: Context): String {
        val prefs = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        return prefs.getString(CLAVE_ORDEN, ORDEN_CODIGO) ?: ORDEN_CODIGO
    }

    // GUARDAR EL ORDEN ELEGIDO
    fun guardarOrden(contexto: Context, orden: String) {
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit {
            putString(CLAVE_ORDEN, orden)
        }
    }
}