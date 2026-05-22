package com.tuapp.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tuapp.model.Event

class EventRepository {

    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    // Obtener todos los eventos
    fun getEvents(onSuccess: (List<Event>) -> Unit, onError: (String) -> Unit) {
        eventsCollection.get()
            .addOnSuccessListener { result ->
                val events = result.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
                onSuccess(events)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al obtener eventos")
            }
    }

    // Obtener eventos de un organizador
    fun getEventsByOrganizer(organizerId: String, onSuccess: (List<Event>) -> Unit, onError: (String) -> Unit) {
        eventsCollection
            .whereEqualTo("organizerId", organizerId)
            .get()
            .addOnSuccessListener { result ->
                val events = result.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
                onSuccess(events)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al obtener eventos")
            }
    }

    // Crear evento
    fun createEvent(event: Event, onSuccess: () -> Unit, onError: (String) -> Unit) {
        eventsCollection.add(event)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al crear evento") }
    }

    // Actualizar evento
    fun updateEvent(event: Event, onSuccess: () -> Unit, onError: (String) -> Unit) {
        eventsCollection.document(event.id)
            .set(event)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al actualizar evento") }
    }

    // Eliminar evento
    fun deleteEvent(eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        eventsCollection.document(eventId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al eliminar evento") }
    }
}
