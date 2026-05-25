package com.example.gestor_comunidad

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.example.gestor_comunidad.data.model.User
import com.example.gestor_comunidad.data.repository.AuthRepository
import com.example.gestor_comunidad.data.repository.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Button
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.view.View

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserCommunity: TextView
    private lateinit var tvAboutMe: TextView
    private lateinit var btnMenu: ImageButton

    private val authRepository = AuthRepository()
    private val firestoreRepository = FirestoreRepository()

    private lateinit var btnAddEvent: ImageButton
    //private lateinit var btnViewEvent: ImageButton
    private lateinit var btnCreados: Button
    private lateinit var btnAsistire: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var rvProfileEvents: RecyclerView
    private lateinit var tvEmptyEvents: TextView
    private lateinit var profileEventAdapter: EventAdapter
    private val eventRepository = EventRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupDrawer()
        setupListeners()
        loadUserData()
        loadCreatedEvents()
    }

    override fun onStart() {
        super.onStart()
        if (!authRepository.isLoggedIn()) {
            navigateToLogin()
        }
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserCommunity = findViewById(R.id.tvUserCommunity)
        tvAboutMe = findViewById(R.id.tvAboutMe)
        btnMenu = findViewById(R.id.btnMenu)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        //btnViewEvent = findViewById(R.id.btnViewEvent)
        btnCreados = findViewById(R.id.btnCreados)
        btnAsistire = findViewById(R.id.btnAsistire)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        rvProfileEvents = findViewById(R.id.rvProfileEvents)
        tvEmptyEvents = findViewById(R.id.tvEmptyEvents)

        profileEventAdapter = EventAdapter(emptyList()) { event ->
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra(EventActivity.EXTRA_SCREEN, EventActivity.SCREEN_DETAIL)
            intent.putExtra("eventId", event.id)
            startActivity(intent)
        }

        rvProfileEvents.layoutManager = LinearLayoutManager(this)
        rvProfileEvents.adapter = profileEventAdapter
    }

    private fun setupListeners() {
        btnMenu.setOnClickListener {
            drawerLayout.open()
        }
        btnAddEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra(EventActivity.EXTRA_SCREEN, EventActivity.SCREEN_CREATE)
            startActivity(intent)
        }

        /*btnViewEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra(EventActivity.EXTRA_SCREEN, EventActivity.SCREEN_LIST)
            startActivity(intent)
        }*/

        btnCreados.setOnClickListener {
            loadCreatedEvents()
        }

        btnAsistire.setOnClickListener {
            loadAttendingEvents()
        }
    }

    private fun loadUserData() {
        val uid = authRepository.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val result = firestoreRepository.getUser(uid)
            result.onSuccess { user ->
                displayUserData(user)
            }.onFailure {
                Toast.makeText(
                    this@ProfileActivity,
                    getString(R.string.error_cargar_perfil),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayUserData(user: User) {
        val fullName = "${user.nombre} ${user.apellido}".trim().uppercase()
        tvUserName.text = fullName.ifEmpty { getString(R.string.sin_nombre) }
        tvUserEmail.text = user.correo.ifEmpty { getString(R.string.sin_correo) }
        tvUserCommunity.text = user.comunidad.ifEmpty { getString(R.string.sin_comunidad) }
        tvAboutMe.text = "Aquí encontrarás tu historial de eventos creados y a los que asistirás..."
    }

    private fun showLogoutDialog() {
        val options = arrayOf(getString(R.string.cerrar_sesion), getString(R.string.cancelar))
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_opciones))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> logout()
                }
            }
            .show()
    }

    private fun logout() {
        authRepository.signOut()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                CredentialManager.create(this@ProfileActivity)
                    .clearCredentialState(
                        androidx.credentials.ClearCredentialStateRequest()
                    )
            } catch (_: ClearCredentialException) {}
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    drawerLayout.close()
                }

                R.id.nav_events -> {
                    val intent = Intent(this, EventActivity::class.java)
                    intent.putExtra(EventActivity.EXTRA_SCREEN, EventActivity.SCREEN_LIST)
                    startActivity(intent)
                }

                R.id.nav_create_event -> {
                    val intent = Intent(this, EventActivity::class.java)
                    intent.putExtra(EventActivity.EXTRA_SCREEN, EventActivity.SCREEN_CREATE)
                    startActivity(intent)
                }

                R.id.nav_logout -> {
                    logout()
                }
            }

            drawerLayout.close()
            true
        }
    }

    private fun loadCreatedEvents() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        btnCreados.setBackgroundResource(R.drawable.bg_tab_active)
        btnAsistire.setBackgroundResource(R.drawable.bg_tab_inactive)

        eventRepository.getEventsByOrganizer(
            organizerId = uid,
            onSuccess = { events ->
                profileEventAdapter.updateList(events)
                tvEmptyEvents.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                rvProfileEvents.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            },
            onError = { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadAttendingEvents() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        btnCreados.setBackgroundResource(R.drawable.bg_tab_inactive)
        btnAsistire.setBackgroundResource(R.drawable.bg_tab_active)

        eventRepository.getEventsUserWillAttend(
            userId = uid,
            onSuccess = { events ->
                profileEventAdapter.updateList(events)
                tvEmptyEvents.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                rvProfileEvents.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            },
            onError = { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
