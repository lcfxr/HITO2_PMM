package com.example.act2_ud1

data class Linea(//HEMOS CREADO UNA CLASE LINEA EN EL ARCHIVO DONDE ESTA MAIN ACTIVITI EN KOTLIN
    val id: Int,//ID DE LINEA
    val codigo: String,//EL NUMERO DE LA LINEA
    val recorrido: String//EL RECORRIDO DE LA LINEA
){
    // PARA QUE CUANDO LA LISTA QUIERA MOSTRAR UNA LINEA QUE SE VEA SOLO EL CODIGO
    override fun toString(): String = codigo
}
