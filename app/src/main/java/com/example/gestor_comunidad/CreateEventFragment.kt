package com.example.gestor_comunidad

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.Toolbar
import com.example.gestor_comunidad.data.repository.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.gestor_comunidad.R
import java.util.Calendar

class CreateEventFragment : Fragment() {

    private lateinit var viewModel: EventViewModel
    private lateinit var etName: TextInputEditText
    private lateinit var etPlace: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etDescription: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private var isEditMode = false
    private var eventId = ""
    private var originalOrganizerId = ""
    private var originalAttendeesCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? EventActivity)?.openDrawer()
        }

        viewModel = ViewModelProvider(this)[EventViewModel::class.java]

        // Inicializar vistas
        etName        = view.findViewById(R.id.etName)
        etPlace       = view.findViewById(R.id.etPlace)
        etDate        = view.findViewById(R.id.etDate)
        etTime        = view.findViewById(R.id.etTime)
        actvCategory  = view.findViewById(R.id.actvCategory)
        etDescription = view.findViewById(R.id.etDescription)
        progressBar   = view.findViewById(R.id.progressBar)
        isEditMode = arguments?.getBoolean("isEditMode") ?: false
        eventId = arguments?.getString("eventId") ?: ""
        originalOrganizerId = arguments?.getString("eventOrganizerId") ?: ""
        originalAttendeesCount = arguments?.getInt("eventAttendees") ?: 0

        if (isEditMode) {
            toolbar.title = "Editar Evento"
            view.findViewById<Button>(R.id.btnCreateEvent).text = "Actualizar evento"

            val day = arguments?.getString("eventDay") ?: ""
            val month = arguments?.getString("eventMonth") ?: ""
            val date = if (day.isNotEmpty() && month.isNotEmpty()) {
                "$day/${monthToNumber(month)}/2026"
            } else {
                ""
            }

            etName.setText(arguments?.getString("eventTitle") ?: "")
            etPlace.setText(arguments?.getString("eventLocation") ?: "")
            etDate.setText(date)
            etTime.setText(arguments?.getString("eventStartTime") ?: "")
            etDescription.setText(arguments?.getString("eventDescription") ?: "")
            actvCategory.setText(arguments?.getString("eventCategory") ?: "", false)
        }

        // Dropdown categorías
        val categories = listOf("Deportes", "Cultura", "Educación", "Salud", "Recreación")
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(catAdapter)

        // Selector de fecha
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                etDate.setText("$day/${month + 1}/$year")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Selector de hora
        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val h = if (hour % 12 == 0) 12 else hour % 12
                val m = String.format("%02d", minute)
                etTime.setText("$h:$m $amPm")
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        // Observar loading
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar éxito → regresar
        viewModel.success.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.eventFragmentContainer, EventListFragment())
                .commit()
        }

        // Observar error
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Botón Crear evento → guardar en Firestore
        view.findViewById<Button>(R.id.btnCreateEvent).setOnClickListener {
            if (validateFields()) {
                if (isEditMode) {
                    updateEventInFirestore()
                } else {
                    saveEventToFirestore()
                }
            }
        }

        // Botón Regresar
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.eventFragmentContainer, EventListFragment())
                .commit()
        }
    }

    private fun validateFields(): Boolean {
        if (etName.text.isNullOrBlank()) {
            etName.error = "El nombre es requerido"
            return false
        }
        if (etPlace.text.isNullOrBlank()) {
            etPlace.error = "El lugar es requerido"
            return false
        }
        if (etDate.text.isNullOrBlank()) {
            etDate.error = "La fecha es requerida"
            return false
        }
        if (etTime.text.isNullOrBlank()) {
            etTime.error = "La hora es requerida"
            return false
        }
        return true
    }

    private fun saveEventToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val organizerId = currentUser.uid

        val dateParts = etDate.text.toString().split("/")
        val day = dateParts.getOrNull(0) ?: ""
        val monthNum = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
        val months = listOf("ENE","FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DIC")
        val month = months.getOrElse(monthNum - 1) { "ENE" }

        CoroutineScope(Dispatchers.Main).launch {
            val result = FirestoreRepository().getUser(organizerId)

            result.onSuccess { user ->
                val organizerName = "${user.nombre} ${user.apellido}".trim()
                    .ifEmpty { user.correo }

                val event = Event(
                    title = etName.text.toString().trim(),
                    description = etDescription.text.toString().trim(),
                    day = day,
                    month = month,
                    startTime = etTime.text.toString(),
                    endTime = "",
                    location = etPlace.text.toString().trim(),
                    organizerId = organizerId,
                    organizerName = organizerName,
                    category = actvCategory.text.toString(),
                    attendeesCount = 0
                )

                viewModel.createEvent(event)
            }
        }
    }

    private fun updateEventInFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        val dateParts = etDate.text.toString().split("/")
        val day = dateParts.getOrNull(0) ?: ""
        val monthNum = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
        val months = listOf("ENE","FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DIC")
        val month = months.getOrElse(monthNum - 1) { "ENE" }

        val event = Event(
            id = eventId,
            title = etName.text.toString().trim(),
            description = etDescription.text.toString().trim(),
            day = day,
            month = month,
            startTime = etTime.text.toString(),
            endTime = "",
            location = etPlace.text.toString().trim(),
            organizerId = originalOrganizerId.ifEmpty { currentUser?.uid ?: "" },
            organizerName = currentUser?.displayName ?: "Organizador",
            category = actvCategory.text.toString(),
            attendeesCount = originalAttendeesCount
        )

        viewModel.updateEvent(event)
    }

    private fun monthToNumber(month: String): String {
        return when (month.uppercase()) {
            "ENE" -> "1"
            "FEB" -> "2"
            "MAR" -> "3"
            "ABR" -> "4"
            "MAY" -> "5"
            "JUN" -> "6"
            "JUL" -> "7"
            "AGO" -> "8"
            "SEP" -> "9"
            "OCT" -> "10"
            "NOV" -> "11"
            "DIC" -> "12"
            else -> "1"
        }
    }
    private fun goToEventList() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.eventFragmentContainer, EventListFragment())
            .commit()
    }
}
