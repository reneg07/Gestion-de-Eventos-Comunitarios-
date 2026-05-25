package com.example.gestor_comunidad

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val day: String = "",       // "15"
    val month: String = "",     // "NOV"
    val startTime: String = "", // "10:00 AM"
    val endTime: String = "",   // "11:00 AM"
    val location: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val category: String = "",
    val attendeesCount: Int = 0
)
