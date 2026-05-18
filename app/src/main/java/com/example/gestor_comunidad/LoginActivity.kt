package com.example.gestor_comunidad
/* pantalla main o principal login con email y password  tambien google
* los demas aparecen pero por complejidad de implementacion se muestra mensaje proximamente*/

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.gestor_comunidad.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: TextView
    private lateinit var tvCrearCuenta: TextView
    private lateinit var btnGoogle: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var btnTwitter: ImageButton
    private lateinit var btnGithub: ImageButton

    private val authRepository = AuthRepository()
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        credentialManager = CredentialManager.create(this)

        initViews()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        if (authRepository.isLoggedIn()) {
            navigateToProfile()
        }
    }

    private fun initViews() {
        etCorreo = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        tvCrearCuenta = findViewById(R.id.tvCrearCuenta)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        btnTwitter = findViewById(R.id.btnTwitter)
        btnGithub = findViewById(R.id.btnGithub)
    }

    private fun setupListeners() {
        btnIniciarSesion.setOnClickListener { attemptLogin() }
        tvCrearCuenta.setOnClickListener { navigateToRegister() }
        btnGoogle.setOnClickListener { launchGoogleSignIn() }
        btnFacebook.setOnClickListener {
            Toast.makeText(this, getString(R.string.proximamente), Toast.LENGTH_SHORT).show()
        }
        btnTwitter.setOnClickListener {
            Toast.makeText(this, getString(R.string.proximamente), Toast.LENGTH_SHORT).show()
        }
        btnGithub.setOnClickListener {
            Toast.makeText(this, getString(R.string.proximamente), Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptLogin() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        if (!validateFields(correo, contrasena)) return

        btnIniciarSesion.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val result = authRepository.loginWithEmail(correo, contrasena)
            btnIniciarSesion.isEnabled = true
            result.onSuccess {
                navigateToProfile()
            }.onFailure { e ->
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_login, e.localizedMessage),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateFields(correo: String, contrasena: String): Boolean {
        if (correo.isEmpty()) {
            etCorreo.error = getString(R.string.error_campo_vacio)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = getString(R.string.error_correo_invalido)
            return false
        }
        if (contrasena.isEmpty()) {
            etContrasena.error = getString(R.string.error_campo_vacio)
            return false
        }
        return true
    }

    private fun launchGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(result.credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_google_signin), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.onSuccess {
                navigateToProfile()
            }.onFailure { e ->
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_login, e.localizedMessage),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
