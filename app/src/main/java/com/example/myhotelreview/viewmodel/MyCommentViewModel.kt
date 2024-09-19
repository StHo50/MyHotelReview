package com.example.myhotelreview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.model.CommentRepository
import com.example.myhotelreview.model.User
import com.example.myhotelreview.model.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyCommentsViewModel(application: Application) : AndroidViewModel(application) {
    private val commentRepository: CommentRepository = CommentRepository(application)
    private val userRepository = UserRepository(application)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    fun getCommentsForUser(): LiveData<List<Comment>> {
        _isLoading.value = true

        val userId = getCurrentUserId()
        return if (userId != null) {
            _isLoading.value = false
            commentRepository.getCommentsForUser(userId)
        } else {
            _isLoading.value = false
            MutableLiveData(emptyList())
        }
    }


    private fun getCurrentUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
    }

    fun getUserFromRoom(userId: String): LiveData<User?> {
        return userRepository.getUserByIdLive(userId)
    }
}
