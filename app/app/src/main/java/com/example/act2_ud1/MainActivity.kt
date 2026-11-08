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

    // IDS DE LAS OPCIONES DEL MENÚ (PARA SABER CUÁL SE HA PULSADO)
    private val MENU_ORDEN_CODIGO = 1
    private val MENU_ORDEN_RECORRIDO = 2
    private val MENU_AYUDA = 3

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

        // 5. BOTÓN OPCIONES: MENÚ CON ORDENAR Y AYUDA
        btnOpciones.setOnClickListener { boton ->
            val menu = PopupMenu(this, boton)
            val ordenActual = Ajustes.leerOrden(this)

            // 5.1 OPCIONES DE ORDEN: SE MARCA CON ✓ LA QUE ESTÁ ELEGIDA
            val opCodigo = menu.menu.add(0, MENU_ORDEN_CODIGO, 0, R.string.menu_ordenar_codigo)
            opCodigo.isCheckable = true
            opCodigo.isChecked = (ordenActual == Ajustes.ORDEN_CODIGO)

            val opRecorrido = menu.menu.add(0, MENU_ORDEN_RECORRIDO, 1, R.string.menu_ordenar_recorrido)
            opRecorrido.isCheckable = true
            opRecorrido.isChecked = (ordenActual == Ajustes.ORDEN_RECORRIDO)

            // 5.2 OPCIÓN DE AYUDA (LA DE SIEMPRE)
            menu.menu.add(0, MENU_AYUDA, 2, R.string.menu_ayuda)

            // 5.3 QUÉ HACER SEGÚN LA OPCIÓN PULSADA
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

        // 6. BOTÓN CONSORCIO: ABRIR LA PANTALLA DE LÍNEAS REALES (CONSULTA HTTPS)
        findViewById<Button>(R.id.btnConsorcio).setOnClickListener {
            startActivity(Intent(this, ConsorcioActivity::class.java))
        }

        // 7. BOTÓN PARADA MÁS CERCANA: ABRIR LA PANTALLA DE UBICACIÓN
        findViewById<Button>(R.id.btnCerca).setOnClickListener {
            startActivity(Intent(this, CercaActivity::class.java))
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

    // CAMBIAR ORDEN: GUARDA LA PREFERENCIA Y REORDENA LA LISTA AL MOMENTO
    private fun cambiarOrden(orden: String) {
        Ajustes.guardarOrden(this, orden)   // 1. GUARDAR LA ELECCIÓN DEL USUARIO
        recargarLista()                     // 2. VOLVER A PINTAR LA LISTA CON EL ORDEN NUEVO
        Log.d(TAG, "Orden cambiado a: $orden")
    }

    // RECARGAR: LEE LAS LÍNEAS DEL ALMACÉN, LAS ORDENA SEGÚN LA PREFERENCIA Y ACTUALIZA LA PANTALLA
    private fun recargarLista() {
        val guardadas = AlmacenLineas.leer(this)

        // ORDENAR SEGÚN LO QUE ELIGIÓ EL USUARIO (PREFERENCIA GUARDADA EN Ajustes)
        val orden = Ajustes.leerOrden(this)
        val ordenadas = if (orden == Ajustes.ORDEN_RECORRIDO) {
            guardadas.sortedBy { it.recorrido }   // A-Z POR RECORRIDO
        } else {
            guardadas.sortedBy { it.codigo }      // A-Z POR CÓDIGO
        }

        lineas.clear()                                 // 1. VACIAR LA LISTA DE LA PANTALLA
        lineas.addAll(ordenadas)                       // 2. LLENARLA CON LAS LÍNEAS YA ORDENADAS
        adapter.notifyDataSetChanged()                 // 3. AVISAR AL ADAPTADOR PARA QUE SE REDIBUJE
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