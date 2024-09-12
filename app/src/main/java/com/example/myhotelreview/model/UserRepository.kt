package com.example.myhotelreview.model

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {

    private val userDao: UserDao

    init {
        val database = UserDatabase.getDatabase(context)
        userDao = database.userDao()
    }

    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    fun getUserByIdLive(id: String): LiveData<User?> {
        return userDao.getUserByIdLive(id)
    }

    suspend fun getUserById(id: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(id)
        }
    }

    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.updateUser(user)
        }
    }

    suspend fun deleteUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.deleteUser(user)
        }
    }
}