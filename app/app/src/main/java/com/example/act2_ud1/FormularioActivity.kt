package com.example.act2_ud1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class FormularioActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // ETIQUETAS DE LOS DATOS QUE SE DEVUELVEN A LA LISTA
    companion object {
        const val EXTRA_CODIGO = "extra_codigo_nuevo"
        const val EXTRA_RECORRIDO = "extra_recorrido_nuevo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        // 1. CONECTAR XML CON KOTLIN
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etRecorrido = findViewById<EditText>(R.id.etRecorrido)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

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

            // 2.4 TODO CORRECTO: DEVOLVER LOS DATOS A LA LISTA Y CERRAR
            val resultado = Intent()
            resultado.putExtra(EXTRA_CODIGO, codigo)
            resultado.putExtra(EXTRA_RECORRIDO, recorrido)
            setResult(RESULT_OK, resultado)
            Log.d(TAG, "Línea válida, se devuelve: $codigo")
            finish()
        }

        // 3. CANCELAR: CERRAR SIN DEVOLVER NADA
        btnCancelar.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}