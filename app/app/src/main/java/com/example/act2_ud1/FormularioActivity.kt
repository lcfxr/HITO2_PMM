package com.example.act2_ud1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class FormularioActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // ETIQUETA PARA EL ID (SI NO LLEGA ES UNA LINEA NUEVA)
    companion object {
        const val EXTRA_ID_EDITAR = "extra_id_editar"
    }

    // ID A EDITAR (-1 SI ES NUEVA)
    private var idEditar = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        // 1. CONECTAR XML CON KOTLIN
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etRecorrido = findViewById<EditText>(R.id.etRecorrido)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        // 1.1 SI ES EDITAR, CAMBIO EL TITULO Y RELLENO LOS CAMPOS
        idEditar = intent.getIntExtra(EXTRA_ID_EDITAR, -1)
        if (idEditar != -1) {
            findViewById<TextView>(R.id.tvTituloFormulario).setText(R.string.titulo_editar)
            val linea = AlmacenLineas.buscar(this, idEditar)
            // SOLO LA PRIMERA VEZ (SI GIRO LA PANTALLA NO SE PIERDE LO ESCRITO)
            if (linea != null && savedInstanceState == null) {
                etCodigo.setText(linea.codigo)
                etRecorrido.setText(linea.recorrido)
            }
        }

        // 2. GUARDAR
        btnGuardar.setOnClickListener {
            // 2.1 LEER LOS TEXTOS SIN ESPACIOS
            val codigo = etCodigo.text.toString().trim()
            val recorrido = etRecorrido.text.toString().trim()

            // 2.2 VALIDAR
            var primerError: EditText? = null

            if (codigo.length < 2) {
                etCodigo.error = getString(R.string.error_codigo_corto)
                primerError = etCodigo
            }
            if (recorrido.isEmpty()) {
                etRecorrido.error = getString(R.string.error_recorrido_vacio)
                if (primerError == null) primerError = etRecorrido
            }

            // 2.3 SI HAY ERROR, NO SE GUARDA
            if (primerError != null) {
                primerError.requestFocus()
                Log.d(TAG, "Validación fallida")
                return@setOnClickListener
            }

            // 2.4 SI ESTA BIEN, GUARDAR Y CERRAR
            if (idEditar != -1) {
                AlmacenLineas.editar(this, idEditar, codigo, recorrido)   // EDITAR
            } else {
                AlmacenLineas.agregar(this, codigo, recorrido)   // AÑADIR
            }
            setResult(RESULT_OK)
            Log.d(TAG, "Línea guardada: $codigo")
            finish()
        }

        // 3. CANCELAR
        btnCancelar.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}