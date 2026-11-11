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

// PANTALLA DEL CONSORCIO (LINEAS REALES POR HTTPS)
class ConsorcioActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // URL DE LA API, EL 1 ES SEVILLA
    private val URL_LINEAS = "https://api.ctan.es/v1/Consorcios/1/lineas"

    // PIEZAS DE LA PANTALLA
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

        // 2. BUSCAR (BOTON, TECLADO O REINTENTAR)
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

        // 3. VOLVER
        findViewById<Button>(R.id.btnVolverConsorcio).setOnClickListener { finish() }

        // 4. AL ENTRAR CARGO TODAS LAS LINEAS
        buscar()
    }

    // BUSCAR LINEAS
    private fun buscar() {
        val buscado = etBuscar.text.toString().trim()

        // 1. SI NO HAY INTERNET, ERROR
        if (!hayConexion()) {
            mostrarError(getString(R.string.error_sin_conexion))
            Log.d(TAG, "Sin conexión")
            return
        }

        // 2. CARGANDO
        mostrarCargando()

        // 3. DESCARGO EN OTRO HILO PARA QUE NO SE BLOQUEE LA APP
        Thread {
            try {
                val json = descargar(URL_LINEAS)
                val lineas = leerLineas(json, buscado)

                // 4. VUELVO AL HILO PRINCIPAL PARA CAMBIAR LA PANTALLA
                runOnUiThread {
                    if (lineas.isEmpty()) {
                        mostrarVacio(buscado)
                    } else {
                        mostrarDatos(lineas)
                    }
                    Log.d(TAG, "Descarga correcta: ${lineas.size} líneas")
                }
            } catch (e: Exception) {
                // SI ALGO FALLA, ERROR
                Log.e(TAG, "Error al descargar", e)
                runOnUiThread {
                    mostrarError(getString(R.string.error_descarga, e.message ?: e.javaClass.simpleName))
                }
            }
        }.start()
    }

    // MIRA SI HAY WIFI O DATOS
    private fun hayConexion(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val red = cm.activeNetwork ?: return false
        val capacidades = cm.getNetworkCapabilities(red) ?: return false
        return capacidades.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capacidades.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capacidades.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // DESCARGAR LOS DATOS POR HTTPS
    private fun descargar(direccion: String): String {
        val conexion = URI(direccion).toURL().openConnection() as HttpsURLConnection
        conexion.connectTimeout = 10000   // 10 SEGUNDOS MAXIMO
        conexion.readTimeout = 10000
        try {
            val codigo = conexion.responseCode
            if (codigo != HttpsURLConnection.HTTP_OK) {
                throw Exception("HTTP $codigo")
            }
            return conexion.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conexion.disconnect()
        }
    }

    // LEER EL JSON Y QUEDARME CON LAS QUE COINCIDEN
    private fun leerLineas(json: String, buscado: String): List<String> {
        val resultado = mutableListOf<String>()
        val array = JSONObject(json).getJSONArray("lineas")   // SIN TILDE, ASI VIENE EN LA API
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val nombre = obj.optString("nombre")
            val operador = obj.optString("operadores").trim().trimEnd(',')
            // SI NO ESCRIBO NADA, SALEN TODAS
            if (buscado.isEmpty() || nombre.contains(buscado, ignoreCase = true)) {
                resultado.add("$nombre\n$operador")
            }
        }
        return resultado
    }

    // LOS 4 ESTADOS DE LA PANTALLA

    // CARGANDO
    private fun mostrarCargando() {
        pbCargando.visibility = View.VISIBLE
        tvEstado.visibility = View.VISIBLE
        tvEstado.setText(R.string.estado_cargando)
        btnReintentar.visibility = View.GONE
        lvConsorcio.visibility = View.GONE
    }

    // VACIO
    private fun mostrarVacio(buscado: String) {
        pbCargando.visibility = View.GONE
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = getString(R.string.estado_vacio, buscado)
        btnReintentar.visibility = View.GONE
        lvConsorcio.visibility = View.GONE
    }

    // ERROR
    private fun mostrarError(mensaje: String) {
        pbCargando.visibility = View.GONE
        tvEstado.visibility = View.VISIBLE
        tvEstado.text = mensaje
        btnReintentar.visibility = View.VISIBLE
        lvConsorcio.visibility = View.GONE
    }

    // CON DATOS
    private fun mostrarDatos(lineas: List<String>) {
        pbCargando.visibility = View.GONE
        tvEstado.visibility = View.GONE
        btnReintentar.visibility = View.GONE
        lvConsorcio.visibility = View.VISIBLE
        lvConsorcio.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lineas)
    }
}