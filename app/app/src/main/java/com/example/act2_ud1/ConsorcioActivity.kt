package com.example.act2_ud1

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URI
import javax.net.ssl.HttpsURLConnection

// PANTALLA DEL CONSORCIO: DESCARGA LÍNEAS REALES POR HTTPS Y LAS MUESTRA
class ConsorcioActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // DIRECCIÓN DE LA API (HTTPS): CONSORCIO 1 = ÁREA DE SEVILLA
    private val URL_LINEAS = "https://api.ctan.es/v1/Consorcios/1/lineas"

    // PIEZAS DE LA PANTALLA (SE CONECTAN EN onCreate)
    private lateinit var etBuscar: EditText
    private lateinit var pbCargando: ProgressBar
    private lateinit var tvEstado: TextView
    private lateinit var btnReintentar: Button
    private lateinit var lvConsorcio: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consorcio)

        // 1. CONECTAR XML CON KOTLIN
        etBuscar = findViewById(R.id.etBuscar)
        pbCargando = findViewById(R.id.pbCargando)
        tvEstado = findViewById(R.id.tvEstado)
        btnReintentar = findViewById(R.id.btnReintentar)
        lvConsorcio = findViewById(R.id.lvConsorcio)

        // 2. BUSCAR: CON EL BOTÓN, CON LA LUPA DEL TECLADO O CON REINTENTAR
        findViewById<Button>(R.id.btnBuscar).setOnClickListener { buscar() }
        btnReintentar.setOnClickListener { buscar() }
        etBuscar.setOnEditorActionListener { _, accion, _ ->
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                buscar()
                true
            } else {
                false
            }
        }

        // 3. VOLVER A MIS LÍNEAS
        findViewById<Button>(R.id.btnVolverConsorcio).setOnClickListener { finish() }

        // 4. AL ABRIR LA PANTALLA, CARGAR TODAS LAS LÍNEAS
        buscar()
    }

    // ===== LA CONSULTA HTTPS =====

    // BUSCAR: COMPRUEBA LA CONEXIÓN, DESCARGA EN SEGUNDO PLANO Y ENSEÑA EL ESTADO QUE TOQUE
    private fun buscar() {
        val buscado = etBuscar.text.toString().trim()

        // 1. SIN WIFI NI DATOS → ESTADO ERROR (NI SIQUIERA SE INTENTA DESCARGAR)
        if (!hayConexion()) {
            mostrarError(getString(R.string.error_sin_conexion))
            Log.d(TAG, "Sin conexión")
            return
        }

        // 2. ESTADO CARGANDO
        mostrarCargando()

        // 3. HILO SECUNDARIO: LO QUE TARDA (INTERNET) NUNCA EN EL HILO PRINCIPAL
        Thread {
            try {
                val json = descargar(URL_LINEAS)          // 3.1 PEDIR LOS DATOS
                val lineas = leerLineas(json, buscado)    // 3.2 LEER EL JSON Y FILTRAR

                // 4. VOLVER AL HILO PRINCIPAL PARA TOCAR LA PANTALLA
                runOnUiThread {
                    if (lineas.isEmpty()) {
                        mostrarVacio(buscado)             // NINGUNA COINCIDE
                    } else {
                        mostrarDatos(lineas)              // HAY RESULTADOS
                    }
                    Log.d(TAG, "Descarga correcta: ${lineas.size} líneas")
                }
            } catch (e: Exception) {
                // ALGO HA FALLADO (SERVIDOR CAÍDO, TIEMPO AGOTADO, JSON RARO…)
                Log.e(TAG, "Error al descargar", e)
                runOnUiThread {
                    mostrarError(getString(R.string.error_descarga, e.message ?: e.javaClass.simpleName))
                }
            }
        }.start()
    }

    // HAY CONEXIÓN: MIRA SI EL MÓVIL TIENE WIFI, DATOS O CABLE (SIN CONECTARSE TODAVÍA)
    private fun hayConexion(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val red = cm.activeNetwork ?: return false
        val capacidades = cm.getNetworkCapabilities(red) ?: return false
        return capacidades.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capacidades.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capacidades.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // DESCARGAR: HACE LA PETICIÓN HTTPS Y DEVUELVE EL TEXTO DE LA RESPUESTA
    private fun descargar(direccion: String): String {
        val conexion = URI(direccion).toURL().openConnection() as HttpsURLConnection
        conexion.connectTimeout = 10000   // MÁXIMO 10 s PARA CONECTAR
        conexion.readTimeout = 10000      // MÁXIMO 10 s PARA RECIBIR
        try {
            val codigo = conexion.responseCode
            if (codigo != HttpsURLConnection.HTTP_OK) {
                throw Exception("HTTP $codigo")      // EL SERVIDOR HA RESPONDIDO, PERO CON ERROR
            }
            return conexion.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conexion.disconnect()                    // CERRAR SIEMPRE, HAYA IDO BIEN O MAL
        }
    }

    // LEER: CONVIERTE EL JSON EN TEXTOS PARA LA LISTA Y SE QUEDA CON LOS QUE CONTIENEN LO BUSCADO
    private fun leerLineas(json: String, buscado: String): List<String> {
        val resultado = mutableListOf<String>()
        val array = JSONObject(json).getJSONArray("lineas")   // "lineas" SIN TILDE (NOMBRE REAL)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val nombre = obj.optString("nombre")
            val operador = obj.optString("operadores").trim().trimEnd(',')
            // SI NO SE HA ESCRITO NADA, TODAS; SI NO, SOLO LAS QUE CONTIENEN EL TEXTO
            if (buscado.isEmpty() || nombre.contains(buscado, ignoreCase = true)) {
                resultado.add("$nombre\n$operador")
            }
        }
        return resultado
    }

    // ===== LOS 4 ESTADOS DE LA PANTALLA: CADA UNO ENSEÑA SUS PIEZAS Y ESCONDE LAS DEMÁS =====

    // ESTADO CARGANDO: CÍRCULO + "CARGANDO LÍNEAS…"
    private fun mostrarCargando() {
        pbCargando.visibility = View.VISIBLE
        tvEstado.visibility = View.VISIBLE
        tvEstado.setText(R.string.estado_cargando)
        btnReintentar.visibility = View.GONE
        lvConsorcio.visibility = View.GONE
    }

    // ESTADO VACÍO: "NO HAY LÍNEAS QUE PASEN POR …"
    private fun mostrarVacio(buscado: String) {
        pbCargando.visibility = View.GONE
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = getString(R.string.estado_vacio, buscado)
        btnReintentar.visibility = View.GONE
        lvConsorcio.visibility = View.GONE
    }

    // ESTADO ERROR: MENSAJE + BOTÓN REINTENTAR
    private fun mostrarError(mensaje: String) {
        pbCargando.visibility = View.GONE
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = mensaje
        btnReintentar.visibility = View.VISIBLE
        lvConsorcio.visibility = View.GONE
    }

    // ESTADO CON DATOS: SOLO LA LISTA
    private fun mostrarDatos(lineas: List<String>) {
        pbCargando.visibility = View.GONE
        tvEstado.visibility = View.GONE
        btnReintentar.visibility = View.GONE
        lvConsorcio.visibility = View.VISIBLE
        lvConsorcio.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lineas)
    }
}