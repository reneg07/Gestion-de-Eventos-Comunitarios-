package com.example.gestor_comunidad

import com.google.firebase.firestore.FirebaseFirestore

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

    fun getEventsUserWillAttend(
        userId: String,
        onSuccess: (List<Event>) -> Unit,
        onError: (String) -> Unit
    ) {
        eventsCollection.get()
            .addOnSuccessListener { result ->
                val events = mutableListOf<Event>()
                val documents = result.documents

                if (documents.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                var pending = documents.size

                documents.forEach { doc ->
                    doc.reference
                        .collection("attendees")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { attendeeDoc ->
                            if (attendeeDoc.exists()) {
                                doc.toObject(Event::class.java)?.copy(id = doc.id)?.let {
                                    events.add(it)
                                }
                            }

                            pending--
                            if (pending == 0) {
                                onSuccess(events)
                            }
                        }
                        .addOnFailureListener { e ->
                            pending--
                            if (pending == 0) {
                                onSuccess(events)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al obtener eventos asistidos")
            }
    }

    fun isUserAttending(
        eventId: String,
        userId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        eventsCollection.document(eventId)
            .collection("attendees")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.exists())
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al validar asistencia")
            }
    }

    fun joinEvent(
        eventId: String,
        userId: String,
        userName: String,
        userEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val attendeeData = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "userEmail" to userEmail
        )

        val eventRef = eventsCollection.document(eventId)

        db.runBatch { batch ->
            batch.set(
                eventRef.collection("attendees").document(userId),
                attendeeData
            )

            batch.update(
                eventRef,
                "attendeesCount",
                com.google.firebase.firestore.FieldValue.increment(1)
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onError(e.message ?: "Error al unirse al evento")
        }
    }

    fun addReview(
        eventId: String,
        userId: String,
        userName: String,
        comment: String,
        rating: Float,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val reviewData = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "comment" to comment,
            "rating" to rating,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        eventsCollection.document(eventId)
            .collection("reviews")
            .document(userId)
            .set(reviewData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al guardar comentario")
            }
    }

    fun getAttendeesCount(
        eventId: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        eventsCollection.document(eventId)
            .collection("attendees")
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.size())
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al obtener asistentes")
            }
    }

    fun getReviews(
        eventId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        eventsCollection.document(eventId)
            .collection("reviews")
            .get()
            .addOnSuccessListener { result ->
                val reviews = result.documents.mapNotNull { it.data }
                onSuccess(reviews)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al obtener comentarios")
            }
    }

    fun getEventById(
        eventId: String,
        onSuccess: (Event) -> Unit,
        onError: (String) -> Unit
    ) {
        eventsCollection.document(eventId)
            .get()
            .addOnSuccessListener { doc ->
                val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                if (event != null) {
                    onSuccess(event)
                } else {
                    onError("Evento no encontrado")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al obtener evento")
            }
    }
}
