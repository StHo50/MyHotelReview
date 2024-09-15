package com.example.myhotelreview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.model.CommentRepository
import com.example.myhotelreview.model.User
import com.example.myhotelreview.model.UserRepository
import com.google.firebase.auth.FirebaseAuth

class MyCommentsViewModel(application: Application) : AndroidViewModel(application) {
    private val commentRepository: CommentRepository = CommentRepository(application)
    private val userRepository = UserRepository(application)

    fun getCommentsForUser(): LiveData<List<Comment>> {
        val userId = getCurrentUserId()
        return if (userId != null) {
            commentRepository.getCommentsForUser(userId)
        } else {
            // Return empty list if no user is logged in
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
