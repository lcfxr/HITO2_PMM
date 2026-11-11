package com.example.act2_ud1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.content.Intent

class DetalleActivity : AppCompatActivity() {
    // ETIQUETA PARA PASAR EL ID
    companion object {
        const val EXTRA_ID = "extra_id"
    }

    // ID DE LA LINEA (-1 SI NO HAY)
    private var idLinea = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)

        // 1. COGER EL ID
        idLinea = intent.getIntExtra(EXTRA_ID, -1)

        // 2. VOLVER
        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }
        // 3. BORRAR (PIDE CONFIRMACION)
        findViewById<Button>(R.id.btnBorrar).setOnClickListener {
            confirmarBorrado()
        }
        // 4. EDITAR (ABRE EL FORMULARIO CON EL ID)
        findViewById<Button>(R.id.btnEditar).setOnClickListener {
            val intent = Intent(this, FormularioActivity::class.java)
            intent.putExtra(FormularioActivity.EXTRA_ID_EDITAR, idLinea)
            startActivity(intent)
        }
    }

    // CADA VEZ QUE VUELVO A LA PANTALLA, PINTO LOS DATOS
    override fun onResume() {
        super.onResume()
        mostrarLinea()
    }

    // BUSCAR LA LINEA Y PINTAR SUS DATOS
    private fun mostrarLinea() {
        val linea = AlmacenLineas.buscar(this, idLinea)

        // SI YA NO EXISTE, CIERRO
        if (linea == null) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tvCodigo).text = linea.codigo
        findViewById<TextView>(R.id.tvRecorrido).text = linea.recorrido
    }

    // VENTANA DE ¿SEGURO? ANTES DE BORRAR
    private fun confirmarBorrado() {
        val linea = AlmacenLineas.buscar(this, idLinea) ?: return

        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_confirmar_borrar)
            .setMessage(getString(R.string.texto_confirmar_borrar, linea.codigo))
            .setPositiveButton(R.string.btn_borrar) { _, _ ->
                AlmacenLineas.borrar(this, idLinea)
                Toast.makeText(this, R.string.linea_borrada, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}