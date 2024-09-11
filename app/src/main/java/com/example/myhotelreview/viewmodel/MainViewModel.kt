package com.example.myhotelreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myhotelreview.model.FirebaseRepository

class MainViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    private val _welcomeMessage = MutableLiveData<String>()
    val welcomeMessage: LiveData<String> get() = _welcomeMessage

    init {
        fetchWelcomeMessage()
    }

    private fun fetchWelcomeMessage() {
        firebaseRepository.fetchUserData { name, email, profileImageUrl ->
            if (name != null) {
                _welcomeMessage.value = "Welcome, $name"
            } else {
                _welcomeMessage.value = "Welcome!"
            }
        }
    }
}
