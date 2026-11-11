package com.example.act2_ud1

// PARADA CON SU POSICION
data class Parada(
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)

// PARADAS DE EJEMPLO DE SEVILLA
object Paradas {
    val lista = listOf(
        Parada("Sevilla – Plaza de Armas", 37.3911, -6.0029),
        Parada("Sevilla – Prado de San Sebastián", 37.3800, -5.9873),
        Parada("Camas – Centro", 37.4024, -6.0331),
        Parada("Santiponce – Itálica", 37.4430, -6.0420),
        Parada("Castilleja de la Cuesta – Centro", 37.3858, -6.0555),
        Parada("Bormujos – Hospital San Juan de Dios", 37.3726, -6.0716),
        Parada("Valencina de la Concepción – Centro", 37.4166, -6.0743)
    )
}

