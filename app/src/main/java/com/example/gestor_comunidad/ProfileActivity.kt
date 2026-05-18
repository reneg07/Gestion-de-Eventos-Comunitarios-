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

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserCommunity: TextView
    private lateinit var tvAboutMe: TextView
    private lateinit var btnMenu: ImageButton

    private val authRepository = AuthRepository()
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupListeners()
        loadUserData()
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
    }

    private fun setupListeners() {
        btnMenu.setOnClickListener {
            showLogoutDialog()
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
        tvAboutMe.text = user.sobreMi.ifEmpty { getString(R.string.sin_descripcion) }
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
}
