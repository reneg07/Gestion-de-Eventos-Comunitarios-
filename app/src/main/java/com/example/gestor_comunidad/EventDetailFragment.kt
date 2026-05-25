package com.tuapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tuapp.R
import com.tuapp.viewmodel.EventViewModel

class EventDetailFragment : Fragment() {

    private lateinit var viewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EventViewModel::class.java]

        // Recibir datos del evento
        val eventId     = arguments?.getString("eventId") ?: ""
        val title       = arguments?.getString("eventTitle") ?: ""
        val day         = arguments?.getString("eventDay") ?: ""
        val month       = arguments?.getString("eventMonth") ?: ""
        val startTime   = arguments?.getString("eventStartTime") ?: ""
        val endTime     = arguments?.getString("eventEndTime") ?: ""
        val location    = arguments?.getString("eventLocation") ?: ""
        val description = arguments?.getString("eventDescription") ?: ""
        val organizer   = arguments?.getString("eventOrganizer") ?: ""
        val category    = arguments?.getString("eventCategory") ?: ""
        val attendees   = arguments?.getInt("eventAttendees") ?: 0

        // Mostrar datos en pantalla
        view.findViewById<TextView>(R.id.tvEventTitle).text = title.uppercase()
        view.findViewById<TextView>(R.id.tvDetailDay).text = day
        view.findViewById<TextView>(R.id.tvDetailMonth).text = month
        view.findViewById<TextView>(R.id.tvDetailTime).text = "$startTime - $endTime"
        view.findViewById<TextView>(R.id.tvDetailLocation).text = location
        view.findViewById<TextView>(R.id.tvOrganizer).text = organizer
        view.findViewById<TextView>(R.id.tvDescription).text = description
        view.findViewById<Chip>(R.id.chipCategory).text = category
        view.findViewById<Button>(R.id.btnAttendees).text = "👥  $attendees Vecinos Asistirán"

        // Botón Unirme → guarda asistencia en Firestore
        view.findViewById<Button>(R.id.btnJoin).setOnClickListener {
            joinEvent(eventId)
        }
        loadAttendees(eventId)

        view.findViewById<Button>(R.id.btnCancelJoin).setOnClickListener {
            cancelJoinEvent(eventId)
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun joinEvent(eventId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Guardar asistencia en subcolección "attendees" del evento
        val attendeeData = hashMapOf(
            "userId" to currentUser.uid,
            "userName" to (currentUser.displayName ?: "Usuario"),
            "userEmail" to (currentUser.email ?: "")
        )

        db.collection("events")
            .document(eventId)
            .collection("attendees")
            .document(currentUser.uid)
            .set(attendeeData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Te uniste al evento!", Toast.LENGTH_SHORT).show()
                loadAttendees(eventId)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al unirse al evento", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelJoinEvent(eventId: String) {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("events")
            .document(eventId)
            .collection("attendees")
            .document(currentUser.uid)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Asistencia cancelada", Toast.LENGTH_SHORT).show()
                loadAttendees(eventId)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cancelar asistencia", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun loadAttendees(eventId: String) {

        val db = FirebaseFirestore.getInstance()

        db.collection("events")
            .document(eventId)
            .collection("attendees")
            .get()
            .addOnSuccessListener { documents ->

                val attendeesList = StringBuilder()

                for (document in documents) {

                    val userName = document.getString("userName")

                    attendeesList.append("• $userName\n")
                }

                view?.findViewById<TextView>(R.id.tvAttendeesList)?.text =
                    attendeesList.toString()
            }
            .addOnFailureListener {

                Toast.makeText(
                    requireContext(),
                    "Error al cargar asistentes",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
