package com.example.gestor_comunidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.content.Intent

class EventListFragment : Fragment() {

    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: EventAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? EventActivity)?.openDrawer()
        }

        progressBar = view.findViewById(R.id.progressBar)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]

        // Configurar RecyclerView
        val rvEvents = view.findViewById<RecyclerView>(R.id.rvEvents)
        adapter = EventAdapter(emptyList()) { event ->
            navigateToDetail(event)
        }
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = adapter

        // Observar datos de Firestore
        viewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.updateList(events)
        }

        // Observar loading
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Cargar eventos desde Firestore
        viewModel.loadEvents()
    }

    private fun navigateToDetail(event: Event) {
        val fragment = EventDetailFragment().apply {
            arguments = Bundle().apply {
                putString("eventId", event.id)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.eventFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
