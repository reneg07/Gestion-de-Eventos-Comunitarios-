package com.example.gestor_comunidad.data.repository
/* intermediario entre entre firestore y el codigo principal dividir responsabilidades*/

import com.example.gestor_comunidad.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun saveUser(uid: String, user: User) {
        usersCollection.document(uid).set(user).await()
    }

    suspend fun getUser(uid: String): Result<User> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val user = document.toObject(User::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        usersCollection.document(uid).update(updates).await()
    }
}
