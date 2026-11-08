package com.example.act2_ud1

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

// ALMACÉN DE LÍNEAS: GUARDA Y LEE LAS LÍNEAS EN EL MÓVIL
object AlmacenLineas {

    private const val ARCHIVO = "tupesa"
    private const val CLAVE = "lineas"

    // LEER: DEVUELVE LAS LÍNEAS GUARDADAS
    fun leer(contexto: Context): MutableList<Linea> {
        val prefs = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        val guardado = prefs.getString(CLAVE, null)

        // PRIMERA VEZ QUE SE ABRE LA APP: NO HAY NADA GUARDADO → LÍNEAS DE EJEMPLO
        if (guardado == null) {
            return mutableListOf(
                Linea(1, "M-163", "Sevilla – Hospital San Juan"),
                Linea(2, "M-160", "Sevilla – Camas"),
                Linea(3, "M-140", "Sevilla – Santiponce")
            )
        }

        // CONVERTIR EL TEXTO JSON EN UNA LISTA DE LÍNEAS
        val resultado = mutableListOf<Linea>()
        val array = JSONArray(guardado)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            resultado.add(Linea(obj.getInt("id"), obj.getString("codigo"), obj.getString("recorrido")))
        }
        return resultado
    }

    // AGREGAR: AÑADE UNA LÍNEA NUEVA Y LO GUARDA TODO
    fun agregar(contexto: Context, codigo: String, recorrido: String) {
        val lineas = leer(contexto)
        val nuevoId = (lineas.maxOfOrNull { it.id } ?: 0) + 1   // ID NUEVO = EL MAYOR + 1
        lineas.add(Linea(nuevoId, codigo, recorrido))
        guardar(contexto, lineas)
    }

    // GUARDAR: CONVIERTE LA LISTA EN TEXTO JSON
    private fun guardar(contexto: Context, lineas: List<Linea>) {
        val array = JSONArray()
        for (linea in lineas) {
            val obj = JSONObject()
            obj.put("id", linea.id)
            obj.put("codigo", linea.codigo)
            obj.put("recorrido", linea.recorrido)
            array.put(obj)
        }
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit {
            putString(CLAVE, array.toString())
        }
    }
}