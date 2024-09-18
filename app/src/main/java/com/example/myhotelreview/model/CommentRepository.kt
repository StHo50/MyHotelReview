package com.example.myhotelreview.model

import android.content.Context
import androidx.lifecycle.LiveData
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
}


