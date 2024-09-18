package com.example.myhotelreview.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}

