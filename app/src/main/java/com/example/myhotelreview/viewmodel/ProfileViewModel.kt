package com.example.myhotelreview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.model.User
import com.example.myhotelreview.model.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val firebaseRepository = FirebaseRepository()

    fun getUserProfile(): LiveData<User?> {
        val currentUserId = getCurrentUserId()
        viewModelScope.launch {
            val user = userRepository.getUserById(currentUserId)
            if (user == null) {
                firebaseRepository.getUserByIdFromFirestore(currentUserId) { firestoreUser ->
                    firestoreUser?.let {
                        viewModelScope.launch {
                            userRepository.insertUser(it)
                        }
                    }
                }
            }
        }
        return userRepository.getUserByIdLive(currentUserId)
    }

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
            } catch (e: Exception) {
                println("Error updating user in Room: ${e.message}")
            }
        }

        firebaseRepository.updateUserProfile(user) { success, errorMessage ->
            if (!success) {
                errorMessage?.let {
                    println("Error updating profile: $it")
                }
            }
        }
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}
