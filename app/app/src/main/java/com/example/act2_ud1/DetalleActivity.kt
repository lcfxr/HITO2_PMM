package com.example.act2_ud1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.content.Intent

class DetalleActivity : AppCompatActivity() {

    // ETIQUETA DEL DATO QUE LLEGA EN EL INTENT: SOLO EL ID
    companion object {
        const val EXTRA_ID = "extra_id"
    }

    // ID DE LA LÍNEA QUE SE ESTÁ VIENDO (-1 = NINGUNA)
    private var idLinea = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)

        // 1. RECOGER EL ID DEL INTENT (SI NO LLEGA, -1)
        idLinea = intent.getIntExtra(EXTRA_ID, -1)

        // 2. VOLVER A LA LISTA (CIERRA ESTA PANTALLA)
        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }
        // 3. BORRAR: PEDIR CONFIRMACIÓN ANTES DE BORRAR
        findViewById<Button>(R.id.btnBorrar).setOnClickListener {
            confirmarBorrado()
        }
        // 4. EDITAR: ABRIR EL FORMULARIO EN MODO EDICIÓN CON EL ID DE ESTA LÍNEA
        findViewById<Button>(R.id.btnEditar).setOnClickListener {
            val intent = Intent(this, FormularioActivity::class.java)
            intent.putExtra(FormularioActivity.EXTRA_ID_EDITAR, idLinea)
            startActivity(intent)
        }
    }

    // ADA VEZ QUE LA PANTALLA VUELVE A VERSE, PINTAR LOS DATOS ACTUALES
    override fun onResume() {
        super.onResume()
        mostrarLinea()
    }

    //MOSTRAR: BUSCA LA LÍNEA EN EL ALMACÉN Y PINTA SUS DATOS
    private fun mostrarLinea() {
        val linea = AlmacenLineas.buscar(this, idLinea)

        // SI LA LÍNEA YA NO EXISTE, CERRAR EL DETALLE Y VOLVER A LA LISTA
        if (linea == null) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tvCodigo).text = linea.codigo
        findViewById<TextView>(R.id.tvRecorrido).text = linea.recorrido
    }

    // CONFIRMAR BORRADO: VENTANA "¿SEGURO?" Y, SI SE ACEPTA, BORRA Y VUELVE A LA LISTA
    private fun confirmarBorrado() {
        val linea = AlmacenLineas.buscar(this, idLinea) ?: return   // SI NO EXISTE, NO HACER NADA

        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_confirmar_borrar)
            .setMessage(getString(R.string.texto_confirmar_borrar, linea.codigo)) // RELLENA EL %1$s
            .setPositiveButton(R.string.btn_borrar) { _, _ ->
                AlmacenLineas.borrar(this, idLinea)                                 // 1. BORRAR DEL ALMACÉN
                Toast.makeText(this, R.string.linea_borrada, Toast.LENGTH_SHORT).show() // 2. AVISAR
                finish()                                                            // 3. VOLVER A LA LISTA
            }
            .setNegativeButton(R.string.btn_cancelar, null)  // CANCELAR: CIERRA LA VENTANA Y NO HACE NADA
            .show()
    }
}