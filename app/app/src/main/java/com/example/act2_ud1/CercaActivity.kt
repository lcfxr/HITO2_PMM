package com.example.act2_ud1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.location.LocationManagerCompat
import java.util.Locale

// PANTALLA PARADA MÁS CERCANA: PIDE PERMISO DE UBICACIÓN Y CALCULA LA PARADA MÁS PRÓXIMA
class CercaActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // PIEZAS DE LA PANTALLA
    private lateinit var tvResultado: TextView
    private lateinit var btnAbrirAjustes: Button

    // LOS DOS PERMISOS SE PIDEN JUNTOS: EL USUARIO ELIGE PRECISA O APROXIMADA
    private val PERMISOS_UBICACION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // LANZADOR DEL PERMISO: ABRE LA VENTANA DEL SISTEMA Y RECIBE LA RESPUESTA DEL USUARIO
    private val lanzadorPermiso = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { respuesta ->
        // respuesta ES UN MAPA: { PERMISO → true (SÍ) / false (NO) }
        val concedido = respuesta.values.any { it }   // VALE CON QUE DÉ PRECISA O APROXIMADA
        if (concedido) {
            Log.d(TAG, "Permiso de ubicación CONCEDIDO")
            buscarUbicacion()
        } else {
            Log.d(TAG, "Permiso de ubicación DENEGADO")
            atenderNegativa()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cerca)

        // 1. CONECTAR XML CON KOTLIN
        tvResultado = findViewById(R.id.tvResultado)
        btnAbrirAjustes = findViewById(R.id.btnAbrirAjustes)

        // 2. BOTONES
        findViewById<Button>(R.id.btnBuscarParada).setOnClickListener { comprobarPermiso() }
        btnAbrirAjustes.setOnClickListener { abrirAjustes() }
        findViewById<Button>(R.id.btnVolverCerca).setOnClickListener { finish() }
    }

    // AL VOLVER DE AJUSTES: SI YA HAY PERMISO, ESCONDER EL BOTÓN "ABRIR AJUSTES"
    override fun onResume() {
        super.onResume()
        if (tienePermiso()) {
            btnAbrirAjustes.visibility = View.GONE
        }
    }

    // ===== EL FLUJO DEL PERMISO =====

    // COMPROBAR: ¿YA HAY PERMISO? → BUSCAR. ¿YA DIJO QUE NO? → EXPLICAR. SI NO → PREGUNTAR
    private fun comprobarPermiso() {
        when {
            tienePermiso() -> buscarUbicacion()
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> explicarPermiso()
            else -> lanzadorPermiso.launch(PERMISOS_UBICACION)
        }
    }

    // TIENE PERMISO: TRUE SI EL USUARIO YA DIO LA UBICACIÓN PRECISA O LA APROXIMADA
    private fun tienePermiso(): Boolean {
        return PERMISOS_UBICACION.any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // EXPLICAR: VENTANA PROPIA CON EL MOTIVO, ANTES DE VOLVER A PREGUNTAR
    private fun explicarPermiso() {
        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_explicacion_permiso)
            .setMessage(R.string.texto_explicacion_permiso)
            .setPositiveButton(R.string.btn_continuar) { _, _ ->
                lanzadorPermiso.launch(PERMISOS_UBICACION)   // AHORA SÍ, PREGUNTAR
            }
            .setNegativeButton(R.string.btn_cancelar) { _, _ ->
                atenderNegativa()                            // NO QUIERE NI QUE LE PREGUNTEN
            }
            .show()
    }

    // ATENDER LA NEGATIVA: LA APP SIGUE FUNCIONANDO. SI ESTÁ BLOQUEADO, SE OFRECE IR A AJUSTES
    private fun atenderNegativa() {
        val sePuedeVolverAPreguntar =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        if (sePuedeVolverAPreguntar) {
            mostrarMensaje(getString(R.string.cerca_sin_permiso), ajustes = false)
        } else {
            mostrarMensaje(getString(R.string.cerca_permiso_bloqueado), ajustes = true)
        }
    }

    // ABRIR AJUSTES: LLEVA A LA PANTALLA DE AJUSTES DE ESTA APP PARA ACTIVAR EL PERMISO A MANO
    private fun abrirAjustes() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    // ===== LA UBICACIÓN =====

    // BUSCAR UBICACIÓN: PIDE LA POSICIÓN ACTUAL AL SISTEMA Y LUEGO CALCULA LA PARADA MÁS CERCANA
    @SuppressLint("MissingPermission")   // EL PERMISO YA SE HA COMPROBADO ANTES DE LLAMAR AQUÍ
    private fun buscarUbicacion() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 1. ¿ESTÁ ENCENDIDA LA UBICACIÓN DEL MÓVIL?
        if (!LocationManagerCompat.isLocationEnabled(lm)) {
            mostrarMensaje(getString(R.string.cerca_ubicacion_apagada), ajustes = false)
            return
        }

        // 2. ELEGIR LA FUENTE: GPS SI HAY PERMISO PRECISO; SI NO, RED (WIFI Y ANTENAS)
        val proveedor = if (tienePermisoPreciso() && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else {
            LocationManager.NETWORK_PROVIDER
        }

        mostrarMensaje(getString(R.string.cerca_buscando), ajustes = false)

        // 3. PEDIR LA POSICIÓN (LA RESPUESTA LLEGA UN POCO DESPUÉS, EN EL HILO PRINCIPAL)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // ANDROID 11 O MÁS: PIDE UNA POSICIÓN NUEVA
                lm.getCurrentLocation(proveedor, null, mainExecutor) { ubicacion ->
                    mostrarParadaMasCercana(ubicacion)
                }
            } else {
                // MÓVILES MÁS ANTIGUOS: ÚLTIMA POSICIÓN CONOCIDA
                mostrarParadaMasCercana(lm.getLastKnownLocation(proveedor))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al pedir la ubicación", e)
            mostrarMensaje(getString(R.string.cerca_sin_ubicacion), ajustes = false)
        }
    }

    // ¿HAY PERMISO PRECISO? (EL USUARIO PUEDE HABER ELEGIDO SOLO EL APROXIMADO)
    private fun tienePermisoPreciso(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    // PARADA MÁS CERCANA: COMPARA LA UBICACIÓN CON TODAS LAS PARADAS Y SE QUEDA CON LA MENOR DISTANCIA
    private fun mostrarParadaMasCercana(ubicacion: Location?) {
        // SI NO HA LLEGADO NINGUNA POSICIÓN
        if (ubicacion == null) {
            mostrarMensaje(getString(R.string.cerca_sin_ubicacion), ajustes = false)
            return
        }

        // BUSCAR EL MÍNIMO: EMPIEZO CON UNA DISTANCIA "INFINITA" Y ME QUEDO CON LA MENOR
        var masCercana = Paradas.lista[0]
        var menorDistancia = Float.MAX_VALUE
        val distancia = FloatArray(1)   // AQUÍ DEJA ANDROID EL RESULTADO EN METROS

        for (parada in Paradas.lista) {
            Location.distanceBetween(
                ubicacion.latitude, ubicacion.longitude,   // DÓNDE ESTOY
                parada.latitud, parada.longitud,           // DÓNDE ESTÁ LA PARADA
                distancia
            )
            if (distancia[0] < menorDistancia) {
                menorDistancia = distancia[0]
                masCercana = parada
            }
        }

        // DISTANCIA EN TEXTO: METROS SI ES MENOS DE 1 KM; SI NO, KILÓMETROS CON UN DECIMAL
        val textoDistancia = if (menorDistancia < 1000) {
            "${menorDistancia.toInt()} m"
        } else {
            String.format(Locale.getDefault(), "%.1f km", menorDistancia / 1000)
        }

        // SOLO SE ENSEÑA EL NOMBRE Y LA DISTANCIA: NUNCA LAS COORDENADAS (PRIVACIDAD)
        mostrarMensaje(getString(R.string.cerca_resultado, masCercana.nombre, textoDistancia), ajustes = false)
        Log.d(TAG, "Parada más cercana: ${masCercana.nombre} a $textoDistancia")
    }

    // ===== MOSTRAR RESULTADOS =====

    // MOSTRAR MENSAJE: PONE EL TEXTO Y ENSEÑA O ESCONDE EL BOTÓN DE AJUSTES
    private fun mostrarMensaje(texto: String, ajustes: Boolean) {
        tvResultado.visibility = View.VISIBLE
        tvResultado.text = texto
        btnAbrirAjustes.visibility = if (ajustes) View.VISIBLE else View.GONE
    }
}