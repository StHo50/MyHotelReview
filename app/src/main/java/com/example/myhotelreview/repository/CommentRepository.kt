package com.example.myhotelreview.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myhotelreview.model.comment.Comment
import com.example.myhotelreview.model.comment.CommentDao
import com.example.myhotelreview.model.comment.CommentDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentRepository(context: Context) {

    private val commentDao: CommentDao
    private val firebaseRepository = FirebaseRepository()

    init {
        val database = CommentDatabase.getDatabase(context)
        commentDao = database.commentDao()
    }

    suspend fun insertComment(comment: Comment) {
        withContext(Dispatchers.IO) {
            commentDao.insertComment(comment)
            firebaseRepository.insertCommentToFirestore(comment) { success ->
                if (!success) {
                    println("Failed to insert comment into Firestore")
                }
            }
        }
    }

    fun getCommentsForHotel(hotelId: Int): LiveData<List<Comment>> {
        return commentDao.getCommentsForHotel(hotelId)
    }

    fun getCommentsForUser(userId: String): LiveData<List<Comment>> {
        return commentDao.getCommentsForUser(userId)
    }

    suspend fun getCommentsForHotelSync(hotelId: Int): List<Comment> {
        return commentDao.getCommentsForHotelSync(hotelId)
    }

    suspend fun updateComment(comment: Comment) {
        withContext(Dispatchers.IO) {
            commentDao.updateComment(comment)
            firebaseRepository.updateCommentInFirestore(comment) { success ->
                if (!success) {
                    println("Failed to update comment in Firestore")
                }
            }
        }
    }

    suspend fun deleteComment(comment: Comment) {
        withContext(Dispatchers.IO) {
            commentDao.deleteComment(comment)
            firebaseRepository.deleteCommentFromFirestore(comment) { success ->
                if (!success) {
                    println("Failed to delete comment from Firestore")
                }
            }
        }
    }

    suspend fun deleteAllComments() {
        withContext(Dispatchers.IO) {
            commentDao.deleteAllComments() // Delete all comments from Room
            firebaseRepository.deleteAllCommentsFromFirestore() // Delete all comments from Firestore
        }
    }

}


