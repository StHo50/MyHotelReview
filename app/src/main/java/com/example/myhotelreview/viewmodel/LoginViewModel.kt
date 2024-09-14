package com.example.myhotelreview.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.model.UserRepository
import com.example.myhotelreview.view.MainActivity
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseRepository()
    private val userRepository: UserRepository = UserRepository(application)

    fun login(email: String, password: String, context: Context) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            firebaseRepository.loginUser(email, password) { success, error ->
                if (success) {
                    val userId = firebaseRepository.getCurrentUserId()
                    if (userId != null) {
                        // Check if user data exists in Room first
                        viewModelScope.launch {
                            val userInRoom = userRepository.getUserById(userId)
                            if (userInRoom == null) {
                                // If the user is not in Room, fetch from Firestore and insert into Room
                                firebaseRepository.getUserByIdFromFirestore(userId) { firestoreUser ->
                                    firestoreUser?.let {
                                        viewModelScope.launch {
                                            userRepository.insertUser(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, MainActivity::class.java))
                } else {
                    Toast.makeText(context, "Login Failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}