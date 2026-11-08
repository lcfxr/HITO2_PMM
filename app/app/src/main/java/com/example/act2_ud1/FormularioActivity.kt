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

    // ETIQUETA DEL DATO QUE LLEGA: EL ID DE LA LÍNEA A EDITAR (SI NO LLEGA = MODO ALTA)
    companion object {
        const val EXTRA_ID_EDITAR = "extra_id_editar"
    }

    // ID DE LA LÍNEA QUE SE EDITA (-1 = MODO ALTA, LÍNEA NUEVA)
    private var idEditar = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        // 1. CONECTAR XML CON KOTLIN
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etRecorrido = findViewById<EditText>(R.id.etRecorrido)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        // 1.1 MODO EDICIÓN: SI LLEGA UN ID, CAMBIAR EL TÍTULO Y RELLENAR LOS CAMPOS
        idEditar = intent.getIntExtra(EXTRA_ID_EDITAR, -1)
        if (idEditar != -1) {
            findViewById<TextView>(R.id.tvTituloFormulario).setText(R.string.titulo_editar)
            val linea = AlmacenLineas.buscar(this, idEditar)
            // SOLO LA PRIMERA VEZ: AL GIRAR LA PANTALLA, LOS EditText YA CONSERVAN LO ESCRITO
            if (linea != null && savedInstanceState == null) {
                etCodigo.setText(linea.codigo)
                etRecorrido.setText(linea.recorrido)
            }
        }

        // 2. GUARDAR
        btnGuardar.setOnClickListener {

            // 2.1 LEER LOS TEXTOS Y QUITAR ESPACIOS DEL PRINCIPIO Y DEL FINAL
            val codigo = etCodigo.text.toString().trim()
            val recorrido = etRecorrido.text.toString().trim()

            // 2.2 VALIDAR (SE APUNTA EL PRIMER CAMPO QUE FALLA)
            var primerError: EditText? = null

            if (codigo.length < 2) {
                etCodigo.error = getString(R.string.error_codigo_corto)
                primerError = etCodigo
            }
            if (recorrido.isEmpty()) {
                etRecorrido.error = getString(R.string.error_recorrido_vacio)
                if (primerError == null) primerError = etRecorrido
            }

            // 2.3 SI HAY ERROR: CURSOR AL PRIMER CAMPO MALO Y NO SE GUARDA (NO SE BORRA NADA)
            if (primerError != null) {
                primerError.requestFocus()
                Log.d(TAG, "Validación fallida")
                return@setOnClickListener
            }

            // 2.4 TODO CORRECTO: GUARDAR EN EL ALMACÉN (EDITAR O AÑADIR) Y CERRAR
            if (idEditar != -1) {
                AlmacenLineas.editar(this, idEditar, codigo, recorrido)   // MODO EDICIÓN
            } else {
                AlmacenLineas.agregar(this, codigo, recorrido)            // MODO ALTA
            }
            setResult(RESULT_OK)   // AVISAR A QUIEN ME ABRIÓ DE QUE SE HA GUARDADO
            Log.d(TAG, "Línea guardada: $codigo")
            finish()
        }

        // 3. CANCELAR: CERRAR SIN DEVOLVER NADA
        btnCancelar.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}