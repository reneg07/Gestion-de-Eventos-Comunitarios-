package com.example.gestor_comunidad

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestor_comunidad.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmarContrasena: EditText
    private lateinit var btnCrearCuenta: TextView
    private lateinit var tvRegresar: TextView

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etTelefono = findViewById(R.id.etTelefono)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasenaReg)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta)
        tvRegresar = findViewById(R.id.tvRegresar)
    }

    private fun setupListeners() {
        btnCrearCuenta.setOnClickListener { attemptRegister() }
        tvRegresar.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()
        val confirmarContrasena = etConfirmarContrasena.text.toString().trim()

        if (!validateFields(nombre, apellido, telefono, correo, contrasena, confirmarContrasena)) return

        btnCrearCuenta.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val result = authRepository.registerWithEmail(
                email = correo,
                password = contrasena,
                nombre = nombre,
                apellido = apellido,
                telefono = telefono
            )
            btnCrearCuenta.isEnabled = true
            result.onSuccess {
                navigateToProfile()
            }.onFailure { e ->
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.error_registro, e.localizedMessage),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateFields(
        nombre: String,
        apellido: String,
        telefono: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String
    ): Boolean {
        var valid = true

        if (nombre.isEmpty()) {
            etNombre.error = getString(R.string.error_campo_vacio)
            valid = false
        }
        if (apellido.isEmpty()) {
            etApellido.error = getString(R.string.error_campo_vacio)
            valid = false
        }
        if (telefono.isEmpty()) {
            etTelefono.error = getString(R.string.error_campo_vacio)
            valid = false
        }
        if (correo.isEmpty()) {
            etCorreo.error = getString(R.string.error_campo_vacio)
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = getString(R.string.error_correo_invalido)
            valid = false
        }
        if (contrasena.isEmpty()) {
            etContrasena.error = getString(R.string.error_campo_vacio)
            valid = false
        } else if (contrasena.length < 6) {
            etContrasena.error = getString(R.string.error_contrasena_corta)
            valid = false
        }
        if (confirmarContrasena.isEmpty()) {
            etConfirmarContrasena.error = getString(R.string.error_campo_vacio)
            valid = false
        } else if (contrasena != confirmarContrasena) {
            etConfirmarContrasena.error = getString(R.string.error_contrasenas_no_coinciden)
            valid = false
        }
        return valid
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finishAffinity()
    }
}
