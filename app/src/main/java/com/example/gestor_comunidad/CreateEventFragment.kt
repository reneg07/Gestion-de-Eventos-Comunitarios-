package com.tuapp.ui

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
import com.tuapp.R
import com.tuapp.model.Event
import com.tuapp.viewmodel.EventViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EventViewModel::class.java]

        // Inicializar vistas
        etName        = view.findViewById(R.id.etName)
        etPlace       = view.findViewById(R.id.etPlace)
        etDate        = view.findViewById(R.id.etDate)
        etTime        = view.findViewById(R.id.etTime)
        actvCategory  = view.findViewById(R.id.actvCategory)
        etDescription = view.findViewById(R.id.etDescription)
        progressBar   = view.findViewById(R.id.progressBar)

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
            parentFragmentManager.popBackStack()
        }

        // Observar error
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Botón Crear evento → guardar en Firestore
        view.findViewById<Button>(R.id.btnCreateEvent).setOnClickListener {
            if (validateFields()) {
                saveEventToFirestore()
            }
        }

        // Botón Regresar
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
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
        // Obtener usuario actual de Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val organizerId = currentUser?.uid ?: ""
        val organizerName = currentUser?.displayName ?: "Organizador"

        // Parsear día y mes de la fecha
        val dateParts = etDate.text.toString().split("/")
        val day = dateParts.getOrNull(0) ?: ""
        val monthNum = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
        val months = listOf("ENE","FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DIC")
        val month = months.getOrElse(monthNum - 1) { "ENE" }

        val event = Event(
            title         = etName.text.toString().trim(),
            description   = etDescription.text.toString().trim(),
            day           = day,
            month         = month,
            startTime     = etTime.text.toString(),
            endTime       = "",
            location      = etPlace.text.toString().trim(),
            organizerId   = organizerId,
            organizerName = organizerName,
            category      = actvCategory.text.toString(),
            attendeesCount = 0
        )

        // Guardar en Firestore
        viewModel.createEvent(event)
    }
}
