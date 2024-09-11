package com.example.myhotelreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.UserRepository
import com.example.myhotelreview.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Fetch the user by ID from Room database
    fun getUserById(userId: Int): LiveData<User> {
        return userRepository.getUserById(userId)
    }

    // Update the user in Room and sync with Firebase Realtime Database
    fun updateUser(user: User) {
        viewModelScope.launch {
            // Update user in Room database
            userRepository.updateUser(user)
            // Sync updated user with Firebase Realtime Database
            syncUserWithFirebase(user)
        }
    }

    // Sync user details with Firebase Realtime Database
    private fun syncUserWithFirebase(user: User) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.id.toString())
        userRef.setValue(user)
    }
}
