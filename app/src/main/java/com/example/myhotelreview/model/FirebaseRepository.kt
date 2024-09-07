package com.example.myhotelreview.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    fun registerUser(email: String, password: String, name: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = mapOf("email" to email, "name" to name)
                        database.child(userId).setValue(user).addOnCompleteListener { dbTask ->
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

    fun fetchUserData(onComplete: (String?, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            database.child(userId).get().addOnSuccessListener {
                if (it.exists()) {
                    val email = it.child("email").value.toString()
                    val name = it.child("name").value.toString()
                    onComplete(name, email)
                } else {
                    onComplete(null, null)
                }
            }.addOnFailureListener {
                onComplete(null, null)
            }
        } else {
            onComplete(null, null)
        }
    }
}
