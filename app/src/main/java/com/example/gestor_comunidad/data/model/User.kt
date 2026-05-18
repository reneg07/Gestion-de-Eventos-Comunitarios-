package com.example.gestor_comunidad.data.model

/* modelo o tabla para guardar los datos en firestore*/

data class User(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val correo: String = "",
    val comunidad: String = "",
    val sobreMi: String = ""
)
