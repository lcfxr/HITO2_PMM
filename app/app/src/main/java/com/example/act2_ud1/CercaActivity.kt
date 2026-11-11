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

// PANTALLA DE LA PARADA MAS CERCANA
class CercaActivity : AppCompatActivity() {

    private val TAG = "PMDM"

    // PIEZAS DE LA PANTALLA
    private lateinit var tvResultado: TextView
    private lateinit var btnAbrirAjustes: Button

    // PIDO LOS DOS PERMISOS A LA VEZ (PRECISA Y APROXIMADA)
    private val PERMISOS_UBICACION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // AQUI LLEGA LO QUE RESPONDE EL USUARIO AL PERMISO
    private val lanzadorPermiso = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { respuesta ->
        val concedido = respuesta.values.any { it }   // CON UNO DE LOS DOS VALE
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

    // AL VOLVER DE AJUSTES, SI YA HAY PERMISO QUITO EL BOTON
    override fun onResume() {
        super.onResume()
        if (tienePermiso()) {
            btnAbrirAjustes.visibility = View.GONE
        }
    }

    // SI HAY PERMISO BUSCO, SI YA DIJO QUE NO LE EXPLICO, Y SI NO LE PREGUNTO
    private fun comprobarPermiso() {
        when {
            tienePermiso() -> buscarUbicacion()
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> explicarPermiso()
            else -> lanzadorPermiso.launch(PERMISOS_UBICACION)
        }
    }

    // MIRA SI TIENE ALGUNO DE LOS DOS PERMISOS
    private fun tienePermiso(): Boolean {
        return PERMISOS_UBICACION.any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // VENTANA PARA EXPLICAR POR QUE PIDO LA UBICACION
    private fun explicarPermiso() {
        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_explicacion_permiso)
            .setMessage(R.string.texto_explicacion_permiso)
            .setPositiveButton(R.string.btn_continuar) { _, _ ->
                lanzadorPermiso.launch(PERMISOS_UBICACION)
            }
            .setNegativeButton(R.string.btn_cancelar) { _, _ ->
                atenderNegativa()
            }
            .show()
    }

    // SI DICE QUE NO, LA APP SIGUE FUNCIONANDO. SI ESTA BLOQUEADO SALE EL BOTON DE AJUSTES
    private fun atenderNegativa() {
        val sePuedeVolverAPreguntar =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        if (sePuedeVolverAPreguntar) {
            mostrarMensaje(getString(R.string.cerca_sin_permiso), ajustes = false)
        } else {
            mostrarMensaje(getString(R.string.cerca_permiso_bloqueado), ajustes = true)
        }
    }

    // ABRIR LOS AJUSTES DE LA APP
    private fun abrirAjustes() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    // BUSCAR MI UBICACION
    @SuppressLint("MissingPermission")
    private fun buscarUbicacion() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 1. MIRAR SI LA UBICACION ESTA ENCENDIDA
        if (!LocationManagerCompat.isLocationEnabled(lm)) {
            mostrarMensaje(getString(R.string.cerca_ubicacion_apagada), ajustes = false)
            return
        }

        // 2. GPS SI ES PRECISA, RED SI ES APROXIMADA
        val proveedor = if (tienePermisoPreciso() && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else {
            LocationManager.NETWORK_PROVIDER
        }

        mostrarMensaje(getString(R.string.cerca_buscando), ajustes = false)

        // 3. PEDIR LA UBICACION
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // ANDROID 11 O MAS
                lm.getCurrentLocation(proveedor, null, mainExecutor) { ubicacion ->
                    mostrarParadaMasCercana(ubicacion)
                }
            } else {
                // MOVILES MAS VIEJOS
                mostrarParadaMasCercana(lm.getLastKnownLocation(proveedor))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al pedir la ubicación", e)
            mostrarMensaje(getString(R.string.cerca_sin_ubicacion), ajustes = false)
        }
    }

    // MIRA SI EL PERMISO ES PRECISO
    private fun tienePermisoPreciso(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    // BUSCAR LA PARADA MAS CERCANA
    private fun mostrarParadaMasCercana(ubicacion: Location?) {
        // SI NO HA LLEGADO LA UBICACION
        if (ubicacion == null) {
            mostrarMensaje(getString(R.string.cerca_sin_ubicacion), ajustes = false)
            return
        }

        // ME QUEDO CON LA QUE ESTE MAS CERCA
        var masCercana = Paradas.lista[0]
        var menorDistancia = Float.MAX_VALUE
        val distancia = FloatArray(1)   // AQUI SE GUARDAN LOS METROS

        for (parada in Paradas.lista) {
            Location.distanceBetween(
                ubicacion.latitude, ubicacion.longitude,
                parada.latitud, parada.longitud,
                distancia
            )
            if (distancia[0] < menorDistancia) {
                menorDistancia = distancia[0]
                masCercana = parada
            }
        }

        // METROS O KM
        val textoDistancia = if (menorDistancia < 1000) {
            "${menorDistancia.toInt()} m"
        } else {
            String.format(Locale.getDefault(), "%.1f km", menorDistancia / 1000)
        }

        // SOLO ENSEÑO EL NOMBRE Y LA DISTANCIA, NUNCA LAS COORDENADAS
        mostrarMensaje(getString(R.string.cerca_resultado, masCercana.nombre, textoDistancia), ajustes = false)
        Log.d(TAG, "Parada más cercana: ${masCercana.nombre} a $textoDistancia")
    }

    // MOSTRAR EL MENSAJE Y EL BOTON DE AJUSTES SI HACE FALTA
    private fun mostrarMensaje(texto: String, ajustes: Boolean) {
        tvResultado.visibility = View.VISIBLE
        tvResultado.text = texto
        btnAbrirAjustes.visibility = if (ajustes) View.VISIBLE else View.GONE
    }
}