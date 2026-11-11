package com.example.act2_ud1

data class Linea(//HEMOS CREADO UNA CLASE LINEA EN EL ARCHIVO DONDE ESTA MAIN ACTIVITI EN KOTLIN
    val id: Int,//ID DE LINEA
    val codigo: String,//EL NUMERO DE LA LINEA
    val recorrido: String//EL RECORRIDO DE LA LINEA
){
    // LO QUE SE VE EN CADA FILA DE LA LISTA
    override fun toString(): String = "$codigo  ·  $recorrido"
}
