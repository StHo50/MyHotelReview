package com.example.myhotelreview.model.comment

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CommentDao {
    @Insert
    suspend fun insertComment(comment: Comment)

    @Query("SELECT * FROM comments WHERE hotelId = :hotelId ORDER BY timestamp DESC")
    fun getCommentsForHotel(hotelId: Int): LiveData<List<Comment>>

    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCommentsForUser(userId: String): LiveData<List<Comment>>

    @Query("SELECT * FROM comments WHERE hotelId = :hotelId ORDER BY timestamp DESC")
    suspend fun getCommentsForHotelSync(hotelId: Int): List<Comment>

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()

    @Update
    suspend fun updateComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)
}

