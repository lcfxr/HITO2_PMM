package com.example.act2_ud1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleActivity : AppCompatActivity() {

    // ETIQUETAS DE LOS DATOS QUE LLEGAN EN EL INTENT (IGUALES EN LOS DOS LADOS)
    companion object {
        const val EXTRA_CODIGO = "extra_codigo"
        const val EXTRA_RECORRIDO = "extra_recorrido"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)

        // 1. CONECTAR XML CON KOTLIN
        val tvCodigo = findViewById<TextView>(R.id.tvCodigo)
        val tvRecorrido = findViewById<TextView>(R.id.tvRecorrido)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        // 2. RECOGER LOS DATOS DEL INTENT Y MOSTRARLOS
        tvCodigo.text = intent.getStringExtra(EXTRA_CODIGO)
        tvRecorrido.text = intent.getStringExtra(EXTRA_RECORRIDO)

        // 3. VOLVER A LA LISTA (CIERRA ESTA PANTALLA)
        btnVolver.setOnClickListener {
            finish()
        }
    }
}