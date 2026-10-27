package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.model.UserProfileDocument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun getUserProfile(userId: String): Result<UserProfileDocument?> {
        val docPath = "users/$userId"
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                Result.success(snapshot.toObject(UserProfileDocument::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfileDocument?> {
        val docPath = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(UserProfileDocument::class.java) else null
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docPath)
                }
                throw error
            }
    }

    suspend fun saveUserProfile(profile: UserProfileDocument): Result<Unit> {
        val uid = requireUserId()
        require(profile.userId == uid) { "Cannot modify profile belonging to another user" }
        val docRef = db.collection("users").document(uid)

        return try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                // Initial registration with SAT Limited
                val data = mapOf(
                    "userId" to uid,
                    "name" to profile.name.trim().ifEmpty { "SAT Member" },
                    "email" to profile.email.trim(),
                    "organization" to "SAT Limited",
                    "username" to profile.username.trim().ifEmpty { "@${profile.name.lowercase().replace(" ", "_")}" },
                    "status" to profile.status.trim().ifEmpty { "Connected on Kora 🌿" },
                    "phone" to profile.phone.trim(),
                    "photoUrl" to profile.photoUrl,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                docRef.set(data).await()
            } else {
                // Update existing profile (preserves immutable userId, createdAt, organization)
                val updates = mapOf(
                    "name" to profile.name.trim(),
                    "username" to profile.username.trim(),
                    "status" to profile.status.trim(),
                    "phone" to profile.phone.trim(),
                    "photoUrl" to profile.photoUrl,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
