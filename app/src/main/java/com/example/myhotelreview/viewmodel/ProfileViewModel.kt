package com.example.myhotelreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.model.User
import com.example.myhotelreview.model.UserDao
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val firebaseRepository: FirebaseRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    fun fetchUserData() {
        viewModelScope.launch {
            // Try to fetch user from Firebase
            firebaseRepository.fetchUserData { name, email ->
                if (name != null && email != null) {
                    val currentUser = User(id = firebaseRepository.getCurrentUserId() ?: "", name = name, email = email, imageUrl = "")
                    // Save user locally in Room
                    launch {
                        userDao.insertUser(currentUser)
                    }
                    _user.value = currentUser
                }
            }
        }
    }


    fun updateUser(name: String, imageUrl: String) {
        viewModelScope.launch {
            val userId = requireNotNull(firebaseRepository.getCurrentUserId()) { "User ID is null" }

            // Update in Firebase
            firebaseRepository.updateUser(name, imageUrl) { success, message ->
                if (success) {
                    // Update locally in Room
                    val updatedUser = User(
                        id = userId,
                        name = name,
                        email = user.value?.email ?: "",
                        imageUrl = imageUrl
                    )
                    viewModelScope.launch {
                        userDao.updateUser(updatedUser)
                    }
                    _user.value = updatedUser
                }
            }
        }
    }
//    fun getCurrentUserFromRoom() {
//        viewModelScope.launch {
//            val currentUser = userDao.getUserById(firebaseRepository.getCurrentUserId())
//            currentUser?.let { _user.value = it }
//        }
//    }
}
