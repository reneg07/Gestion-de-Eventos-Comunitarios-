package com.example.gestor_comunidad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.example.gestor_comunidad.data.repository.AuthRepository

class EventActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_CREATE = "create"
        const val SCREEN_LIST = "list"
        const val SCREEN_DETAIL = "detail"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        setupDrawer()

        if (savedInstanceState == null) {
            val screen = intent.getStringExtra(EXTRA_SCREEN)

            val fragment = when (screen) {
                SCREEN_CREATE -> CreateEventFragment()
                SCREEN_DETAIL -> EventDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("eventId", intent.getStringExtra("eventId"))
                    }
                }
                else -> EventListFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.eventFragmentContainer, fragment)
                .commit()
        }
    }

    fun openDrawer() {
        drawerLayout.open()
    }

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }

                R.id.nav_events -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.eventFragmentContainer, EventListFragment())
                        .commit()
                }

                R.id.nav_create_event -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.eventFragmentContainer, CreateEventFragment())
                        .commit()
                }

                R.id.nav_logout -> {
                    authRepository.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
            }

            drawerLayout.close()
            true
        }
    }
}