package com.example.act2_ud1

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

// AQUI SE GUARDAN LAS LINEAS EN EL MOVIL (SHAREDPREFERENCES + JSON)
object AlmacenLineas {

    private const val ARCHIVO = "tupesa"
    private const val CLAVE = "lineas"

    // LEER LAS LINEAS GUARDADAS
    fun leer(contexto: Context): MutableList<Linea> {
        val prefs = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        val guardado = prefs.getString(CLAVE, null)

        // LA PRIMERA VEZ NO HAY NADA, ASI QUE PONGO LAS DE EJEMPLO
        if (guardado == null) {
            return mutableListOf(
                Linea(1, "M-163", "Sevilla – Hospital San Juan"),
                Linea(2, "M-160", "Sevilla – Camas"),
                Linea(3, "M-140", "Sevilla – Santiponce")
            )
        }

        // PASAR EL JSON A LISTA
        val resultado = mutableListOf<Linea>()
        val array = JSONArray(guardado)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            resultado.add(Linea(obj.getInt("id"), obj.getString("codigo"), obj.getString("recorrido")))
        }
        return resultado
    }

    // AÑADIR UNA LINEA NUEVA
    fun agregar(contexto: Context, codigo: String, recorrido: String) {
        val lineas = leer(contexto)
        val nuevoId = (lineas.maxOfOrNull { it.id } ?: 0) + 1   // EL ID ES EL MAS ALTO + 1
        lineas.add(Linea(nuevoId, codigo, recorrido))
        guardar(contexto, lineas)
    }

    // BUSCAR UNA LINEA POR SU ID
    fun buscar(contexto: Context, id: Int): Linea? {
        return leer(contexto).find { it.id == id }
    }

    // EDITAR UNA LINEA
    fun editar(contexto: Context, id: Int, codigo: String, recorrido: String) {
        val lineas = leer(contexto)
        val posicion = lineas.indexOfFirst { it.id == id }
        if (posicion != -1) {
            lineas[posicion] = Linea(id, codigo, recorrido)
            guardar(contexto, lineas)
        }
    }

    // BORRAR UNA LINEA
    fun borrar(contexto: Context, id: Int) {
        val lineas = leer(contexto)
        lineas.removeAll { it.id == id }
        guardar(contexto, lineas)
    }

    // GUARDAR LA LISTA COMO JSON
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