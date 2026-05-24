package com.tuapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tuapp.model.Event
import com.tuapp.repository.EventRepository

class EventViewModel : ViewModel() {

    private val repository = EventRepository()

    // LiveData que observan los Fragments
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    // Cargar todos los eventos
    fun loadEvents() {
        _loading.value = true
        repository.getEvents(
            onSuccess = { list ->
                _events.value = list
                _loading.value = false
            },
            onError = { msg ->
                _error.value = msg
                _loading.value = false
            }
        )
    }

    // Cargar eventos por organizador
    fun loadMyEvents(organizerId: String) {
        _loading.value = true
        repository.getEventsByOrganizer(
            organizerId,
            onSuccess = { list ->
                _events.value = list
                _loading.value = false
            },
            onError = { msg ->
                _error.value = msg
                _loading.value = false
            }
        )
    }

    // Crear evento
    fun createEvent(event: Event) {
        _loading.value = true
        repository.createEvent(
            event,
            onSuccess = {
                _success.value = "¡Evento creado exitosamente!"
                _loading.value = false
                loadEvents() // recarga la lista
            },
            onError = { msg ->
                _error.value = msg
                _loading.value = false
            }
        )
    }

    // Actualizar evento
    fun updateEvent(event: Event) {
        _loading.value = true
        repository.updateEvent(
            event,
            onSuccess = {
                _success.value = "¡Evento actualizado!"
                _loading.value = false
                loadEvents()
            },
            onError = { msg ->
                _error.value = msg
                _loading.value = false
            }
        )
    }

    // Eliminar evento
    fun deleteEvent(eventId: String) {
        _loading.value = true
        repository.deleteEvent(
            eventId,
            onSuccess = {
                _success.value = "Evento eliminado"
                _loading.value = false
                loadEvents()
            },
            onError = { msg ->
                _error.value = msg
                _loading.value = false
            }
        )
    }
}
