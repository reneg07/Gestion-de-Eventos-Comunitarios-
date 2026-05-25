package com.example.gestor_comunidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.gestor_comunidad.data.repository.FirestoreRepository
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventDetailFragment : Fragment() {

    private val eventRepository = EventRepository()

    private lateinit var btnJoin: Button
    private lateinit var btnAttendees: Button
    private lateinit var btnEditEvent: Button
    private lateinit var btnDeleteEvent: Button
    private lateinit var layoutReviews: LinearLayout

    private lateinit var currentEvent: Event

    private var eventId: String = ""
    private var organizerId: String = ""
    private var attendeesCount: Int = 0

    private var eventTitle = ""
    private var eventDay = ""
    private var eventMonth = ""
    private var eventStartTime = ""
    private var eventEndTime = ""
    private var eventLocation = ""
    private var eventDescription = ""
    private var eventCategory = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            (activity as? EventActivity)?.openDrawer()
        }

        btnJoin = view.findViewById(R.id.btnJoin)
        btnAttendees = view.findViewById(R.id.btnAttendees)
        btnEditEvent = view.findViewById(R.id.btnEditEvent)
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent)
        layoutReviews = view.findViewById(R.id.layoutReviews)

        eventId = arguments?.getString("eventId") ?: ""

        if (eventId.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró el evento", Toast.LENGTH_SHORT).show()
            return
        }

        loadEventDetail()
    }

    private fun loadEventDetail() {
        eventRepository.getEventById(
            eventId = eventId,
            onSuccess = { event ->
                currentEvent = event

                paintEvent(event)
                configureUserActions(event)
                loadRealAttendeesCount()
                loadReviews()
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun paintEvent(event: Event) {
        eventTitle = event.title
        eventDay = event.day
        eventMonth = event.month
        eventStartTime = event.startTime
        eventEndTime = event.endTime
        eventLocation = event.location
        eventDescription = event.description
        eventCategory = event.category
        organizerId = event.organizerId
        attendeesCount = event.attendeesCount

        view?.findViewById<TextView>(R.id.tvEventTitle)?.text =
            event.title.uppercase()

        view?.findViewById<TextView>(R.id.tvDetailDay)?.text =
            event.day

        view?.findViewById<TextView>(R.id.tvDetailMonth)?.text =
            event.month

        view?.findViewById<TextView>(R.id.tvDetailTime)?.text =
            "${event.startTime} - ${event.endTime}"

        view?.findViewById<TextView>(R.id.tvDetailLocation)?.text =
            event.location

        view?.findViewById<TextView>(R.id.tvOrganizer)?.text =
            event.organizerName

        view?.findViewById<TextView>(R.id.tvDescription)?.text =
            event.description

        view?.findViewById<Chip>(R.id.chipCategory)?.text =
            event.category

        updateAttendeesButton()
    }

    private fun configureUserActions(event: Event) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val isOrganizer = currentUser.uid == event.organizerId

        if (isOrganizer) {

            btnJoin.visibility = View.GONE

            btnEditEvent.visibility = View.VISIBLE
            btnDeleteEvent.visibility = View.VISIBLE

            btnEditEvent.setOnClickListener {
                openEditEvent()
            }

            btnDeleteEvent.setOnClickListener {
                confirmDeleteEvent()
            }

            return
        }

        btnEditEvent.visibility = View.GONE
        btnDeleteEvent.visibility = View.GONE
        btnJoin.visibility = View.VISIBLE

        eventRepository.isUserAttending(
            eventId = event.id,
            userId = currentUser.uid,
            onSuccess = { attending ->

                if (attending) {

                    btnJoin.text = "Comentar y calificar"
                    btnJoin.isEnabled = true

                    btnJoin.setOnClickListener {
                        showReviewDialog()
                    }

                } else {

                    btnJoin.text = "Unirme"
                    btnJoin.isEnabled = true

                    btnJoin.setOnClickListener {
                        joinCurrentEvent()
                    }
                }
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun joinCurrentEvent() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        btnJoin.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {

            val result = FirestoreRepository().getUser(currentUser.uid)

            result.onSuccess { user ->

                val userName = "${user.nombre} ${user.apellido}".trim()
                    .ifEmpty { user.correo }

                eventRepository.joinEvent(
                    eventId = eventId,
                    userId = currentUser.uid,
                    userName = userName,
                    userEmail = currentUser.email ?: "",
                    onSuccess = {

                        attendeesCount += 1
                        updateAttendeesButton()

                        btnJoin.text = "Comentar y calificar"
                        btnJoin.isEnabled = true

                        btnJoin.setOnClickListener {
                            showReviewDialog()
                        }

                        Toast.makeText(
                            requireContext(),
                            "¡Te uniste al evento!",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadRealAttendeesCount()
                    },
                    onError = { msg ->

                        btnJoin.isEnabled = true

                        Toast.makeText(
                            requireContext(),
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    private fun updateAttendeesButton() {
        btnAttendees.text =
            "👥 $attendeesCount Vecinos asistirán"
    }

    private fun loadRealAttendeesCount() {
        eventRepository.getAttendeesCount(
            eventId = eventId,
            onSuccess = { count ->
                attendeesCount = count
                updateAttendeesButton()
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun openEditEvent() {

        val fragment = CreateEventFragment().apply {

            arguments = Bundle().apply {

                putBoolean("isEditMode", true)

                putString("eventId", eventId)
                putString("eventTitle", eventTitle)
                putString("eventDay", eventDay)
                putString("eventMonth", eventMonth)
                putString("eventStartTime", eventStartTime)
                putString("eventEndTime", eventEndTime)
                putString("eventLocation", eventLocation)
                putString("eventDescription", eventDescription)
                putString("eventCategory", eventCategory)
                putString("eventOrganizerId", organizerId)
                putInt("eventAttendees", attendeesCount)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.eventFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDeleteEvent() {

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar evento")
            .setMessage("¿Seguro que deseas eliminar este evento?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteEvent()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteEvent() {

        eventRepository.deleteEvent(
            eventId = eventId,
            onSuccess = {

                Toast.makeText(
                    requireContext(),
                    "Evento eliminado",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.eventFragmentContainer,
                        EventListFragment()
                    )
                    .commit()
            },
            onError = { msg ->

                Toast.makeText(
                    requireContext(),
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun showReviewDialog() {

        val dialogView =
            layoutInflater.inflate(R.layout.dialog_event_review, null)

        val ratingBar =
            dialogView.findViewById<android.widget.RatingBar>(R.id.ratingBar)

        val etComment =
            dialogView.findViewById<android.widget.EditText>(R.id.etComment)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Comentar y calificar")
            .setView(dialogView)

            .setPositiveButton("Guardar") { _, _ ->

                val currentUser =
                    FirebaseAuth.getInstance().currentUser
                        ?: return@setPositiveButton

                val comment =
                    etComment.text.toString().trim()

                val rating =
                    ratingBar.rating

                if (comment.isEmpty() || rating == 0f) {

                    Toast.makeText(
                        requireContext(),
                        "Agrega comentario y calificación",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                CoroutineScope(Dispatchers.Main).launch {

                    val result =
                        FirestoreRepository().getUser(currentUser.uid)

                    result.onSuccess { user ->

                        val userName =
                            "${user.nombre} ${user.apellido}".trim()
                                .ifEmpty { user.correo }

                        eventRepository.addReview(
                            eventId = eventId,
                            userId = currentUser.uid,
                            userName = userName,
                            comment = comment,
                            rating = rating,
                            onSuccess = {

                                Toast.makeText(
                                    requireContext(),
                                    "Comentario guardado",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadReviews()
                            },
                            onError = { msg ->

                                Toast.makeText(
                                    requireContext(),
                                    msg,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadReviews() {

        eventRepository.getReviews(
            eventId = eventId,
            onSuccess = { reviews ->

                layoutReviews.removeAllViews()

                if (reviews.isEmpty()) {

                    val emptyText = TextView(requireContext()).apply {
                        text = "Aún no hay comentarios para este evento."
                        textSize = 14f
                    }

                    layoutReviews.addView(emptyText)
                    return@getReviews
                }

                reviews.forEach { review ->

                    val userName =
                        review["userName"]?.toString() ?: "Usuario"

                    val comment =
                        review["comment"]?.toString() ?: ""

                    val rating =
                        review["rating"]?.toString() ?: "0"

                    val reviewText =
                        TextView(requireContext()).apply {

                            text =
                                "⭐ $rating/5\n$userName\n$comment"

                            textSize = 14f

                            setPadding(0, 12, 0, 12)
                        }

                    layoutReviews.addView(reviewText)
                }
            },
            onError = { msg ->

                Toast.makeText(
                    requireContext(),
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}