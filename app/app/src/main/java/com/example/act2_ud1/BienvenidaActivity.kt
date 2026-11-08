package com.example.act2_ud1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BienvenidaActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        Log.d(TAG, "Estado: onCreate - Pantalla de bienvenida")

        // 1. CONECTAR XML CON KOTLIN
        val btnVerLineas = findViewById<Button>(R.id.btnVerLineas)

        // 2. AL PULSAR, ABRIR EL LISTADO DE LÍNEAS
        btnVerLineas.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}