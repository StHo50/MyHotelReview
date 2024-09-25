package com.example.myhotelreview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.repository.FirebaseRepository
import com.example.myhotelreview.model.user.User
import com.example.myhotelreview.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val firebaseRepository = FirebaseRepository()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    fun getUserProfile(): LiveData<User?> {
        _isLoading.postValue(true)
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
                    _isLoading.postValue(false)
                }
            } else {
                _isLoading.postValue(false)
            }
        }
        return userRepository.getUserByIdLive(currentUserId)
    }

    fun saveUserProfile(user: User) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _isLoading.postValue(false)
                println("Error updating user in Room: ${e.message}")
            }
        }

        firebaseRepository.updateUserProfile(user) { success, errorMessage ->
            _isLoading.postValue(false)
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
