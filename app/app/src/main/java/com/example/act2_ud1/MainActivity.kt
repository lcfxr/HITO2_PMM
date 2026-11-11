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

    // IDS DEL MENU
    private val MENU_ORDEN_CODIGO = 1
    private val MENU_ORDEN_RECORRIDO = 2
    private val MENU_AYUDA = 3

    // LISTA DE LA PANTALLA
    private val lineas = mutableListOf<Linea>()

    // ADAPTADOR
    private lateinit var adapter: ArrayAdapter<Linea>

    // ABRE EL FORMULARIO Y ESPERA A QUE SE GUARDE
    private val lanzadorFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            // SOLO AVISO DE QUE SE HA GUARDADO
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

        // 3. AL TOCAR UNA LINEA, ABRIR EL DETALLE
        lvLineas.setOnItemClickListener { _, _, position, _ ->
            val linea = lineas[position]
            val intent = Intent(this, DetalleActivity::class.java)
            intent.putExtra(DetalleActivity.EXTRA_ID, linea.id)   // SOLO PASO EL ID
            Log.d(TAG, "Abriendo detalle de ${linea.codigo}")
            startActivity(intent)
        }

        // 4. BOTON AÑADIR
        btnAnadir.setOnClickListener {
            lanzadorFormulario.launch(Intent(this, FormularioActivity::class.java))
        }

        // 5. BOTON OPCIONES
        btnOpciones.setOnClickListener { boton ->
            val menu = PopupMenu(this, boton)
            val ordenActual = Ajustes.leerOrden(this)

            // 5.1 ORDENAR (SALE MARCADA LA ELEGIDA)
            val opCodigo = menu.menu.add(0, MENU_ORDEN_CODIGO, 0, R.string.menu_ordenar_codigo)
            opCodigo.isCheckable = true
            opCodigo.isChecked = (ordenActual == Ajustes.ORDEN_CODIGO)

            val opRecorrido = menu.menu.add(0, MENU_ORDEN_RECORRIDO, 1, R.string.menu_ordenar_recorrido)
            opRecorrido.isCheckable = true
            opRecorrido.isChecked = (ordenActual == Ajustes.ORDEN_RECORRIDO)

            // 5.2 AYUDA
            menu.menu.add(0, MENU_AYUDA, 2, R.string.menu_ayuda)

            // 5.3 QUE HACE CADA OPCION
            menu.setOnMenuItemClickListener { opcion ->
                when (opcion.itemId) {
                    MENU_ORDEN_CODIGO -> cambiarOrden(Ajustes.ORDEN_CODIGO)
                    MENU_ORDEN_RECORRIDO -> cambiarOrden(Ajustes.ORDEN_RECORRIDO)
                    MENU_AYUDA -> mostrarAyuda()
                }
                true
            }
            menu.show()
        }

        // 6. BOTON CONSORCIO
        findViewById<Button>(R.id.btnConsorcio).setOnClickListener {
            startActivity(Intent(this, ConsorcioActivity::class.java))
        }

        // 7. BOTON PARADA MAS CERCANA
        findViewById<Button>(R.id.btnCerca).setOnClickListener {
            startActivity(Intent(this, CercaActivity::class.java))
        }
    }

    // VENTANA DE AYUDA
    private fun mostrarAyuda() {
        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_ayuda)
            .setMessage(R.string.texto_ayuda)
            .setPositiveButton(R.string.btn_entendido, null)
            .show()
        Log.d(TAG, "Mostrando ayuda")
    }

    // CAMBIAR EL ORDEN Y GUARDARLO
    private fun cambiarOrden(orden: String) {
        Ajustes.guardarOrden(this, orden)
        recargarLista()
        Log.d(TAG, "Orden cambiado a: $orden")
    }

    // RECARGAR LA LISTA CON EL ORDEN ELEGIDO
    private fun recargarLista() {
        val guardadas = AlmacenLineas.leer(this)

        val orden = Ajustes.leerOrden(this)
        val ordenadas = if (orden == Ajustes.ORDEN_RECORRIDO) {
            guardadas.sortedBy { it.recorrido }
        } else {
            guardadas.sortedBy { it.codigo }
        }

        lineas.clear()
        lineas.addAll(ordenadas)
        adapter.notifyDataSetChanged()
        Log.d(TAG, "Lista recargada: ${lineas.size} líneas, orden: $orden")
    }

    //CICLO DE VIDA

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "La app es visible")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "La app está en primer plano")
        recargarLista()   // CADA VEZ QUE VUELVO, LEO LO GUARDADO
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