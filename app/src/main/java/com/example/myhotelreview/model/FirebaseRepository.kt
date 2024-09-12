package com.example.myhotelreview.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerUser(email: String, password: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = hashMapOf(
                            "email" to email,
                            "name" to name
                        )
                        firestore.collection("Users").document(userId)
                            .set(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onComplete(true, null)
                                } else {
                                    onComplete(false, dbTask.exception?.message)
                                }
                            }
                    } else {
                        onComplete(false, "User ID not found")
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun getUserByIdFromFirestore(userId: String, onComplete: (User?) -> Unit) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val imageUrl = document.getString("imageUrl")

                    val user = User(
                        id = userId,
                        name = name,
                        email = email,
                        imageUrl = imageUrl
                    )
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun updateUserProfile(user: User, onComplete: (Boolean, String?) -> Unit) {
        val userId = user.id
        val userUpdates = mutableMapOf<String, Any>(
            "name" to user.name,
            "email" to user.email
        )

        if (!user.imageUrl.isNullOrEmpty()) {
            userUpdates["imageUrl"] = user.imageUrl
        }

        firestore.collection("Users").document(userId)
            .update(userUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

}