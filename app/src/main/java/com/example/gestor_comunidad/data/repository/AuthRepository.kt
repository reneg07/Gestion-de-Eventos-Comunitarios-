package com.example.gestor_comunidad.data.repository

/* intermediario entre entre fireauth y el codigo principal dividir responsabilidades*/

import com.example.gestor_comunidad.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val currentUser get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun loginWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID vacío"))
            val user = User(
                uid = uid,
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                correo = email
            )
            FirestoreRepository().saveUser(uid, user)
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID vacío"))
            val isNewUser = result.additionalUserInfo?.isNewUser == true
            if (isNewUser) {
                val user = User(
                    uid = uid,
                    nombre = result.user?.displayName?.split(" ")?.firstOrNull() ?: "",
                    apellido = result.user?.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                    correo = result.user?.email ?: ""
                )
                FirestoreRepository().saveUser(uid, user)
            }
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
