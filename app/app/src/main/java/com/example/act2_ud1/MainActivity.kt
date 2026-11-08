package com.example.act2_ud1

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // LISTA QUE SE VE EN PANTALLA: EMPIEZA VACÍA Y SE RELLENA DESDE EL ALMACÉN
    private val lineas = mutableListOf<Linea>()

    // ADAPTADOR: CONVIERTE CADA LÍNEA EN UNA FILA (SE CREA EN onCreate)
    private lateinit var adapter: ArrayAdapter<Linea>

    // LANZADOR DEL FORMULARIO: LO ABRE Y ESPERA LA LÍNEA NUEVA
    private val lanzadorFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            // EL FORMULARIO YA HA VALIDADO Y GUARDADO: SOLO AVISAMOS AL USUARIO
            Toast.makeText(this, R.string.linea_guardada, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "El formulario ha guardado una línea")
        } else {
            Log.d(TAG, "Formulario cancelado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "Estado: onCreate - La app se está creando")

        // 1. CONECTAR XML CON KOTLIN
        val lvLineas = findViewById<ListView>(R.id.lvLineas)
        val btnAnadir = findViewById<Button>(R.id.btnAnadir)
        val btnOpciones = findViewById<Button>(R.id.btnOpciones)

        // 2. UNIR LOS DATOS CON LA LISTA DE LA PANTALLA
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lineas)
        lvLineas.adapter = adapter

        // 3. AL TOCAR UNA LÍNEA, ABRIR EL DETALLE CON SUS DATOS
        lvLineas.setOnItemClickListener { _, _, position, _ ->
            val linea = lineas[position]
            val intent = Intent(this, DetalleActivity::class.java)
            intent.putExtra(DetalleActivity.EXTRA_ID, linea.id) // SOLO EL ID: EL DETALLE BUSCA LA LÍNEA EN EL ALMACÉN
            Log.d(TAG, "Abriendo detalle de ${linea.codigo}")
            startActivity(intent)
        }

        // 4. BOTÓN AÑADIR: ABRIR EL FORMULARIO
        btnAnadir.setOnClickListener {
            lanzadorFormulario.launch(Intent(this, FormularioActivity::class.java))
        }

        // 5. BOTÓN OPCIONES: MENÚ CON LA ACCIÓN AYUDA
        btnOpciones.setOnClickListener { boton ->
            val menu = PopupMenu(this, boton)
            menu.menu.add(getString(R.string.menu_ayuda))
            menu.setOnMenuItemClickListener {
                mostrarAyuda()
                true
            }
            menu.show()
        }
    }

    // VENTANA DE AYUDA: EXPLICA CÓMO USAR LA LISTA
    private fun mostrarAyuda() {
        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_ayuda)
            .setMessage(R.string.texto_ayuda)
            .setPositiveButton(R.string.btn_entendido, null)
            .show()
        Log.d(TAG, "Mostrando ayuda")
    }

    // RECARGAR: LEE LAS LÍNEAS DEL ALMACÉN Y ACTUALIZA LA LISTA DE LA PANTALLA
    private fun recargarLista() {
        lineas.clear()                                 // 1. VACIAR LA LISTA DE LA PANTALLA
        lineas.addAll(AlmacenLineas.leer(this))        // 2. LLENARLA CON LO GUARDADO
        adapter.notifyDataSetChanged()                 // 3. AVISAR AL ADAPTADOR PARA QUE SE REDIBUJE
        Log.d(TAG, "Lista recargada: ${lineas.size} líneas")
    }

    //CICLO DE VIDA

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "La app es visible")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "La app está en primer plano")
        recargarLista() // CADA VEZ QUE VOLVEMOS A ESTA PANTALLA, LEER LO GUARDADO
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "La app va a segundo plano")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "La app ya no es visible")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "La app se va a cerrar por completo")
    }
}