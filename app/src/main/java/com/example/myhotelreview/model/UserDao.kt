package com.example.myhotelreview.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    // Query to get user details by user ID
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): LiveData<User>

    // Update user details
    @Update
    suspend fun updateUser(user: User)
}

