package com.example.myhotelreview.viewmodel

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.view.MainActivity

class LoginViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    fun login(email: String, password: String, context: Context) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            firebaseRepository.loginUser(email, password) { success, error ->
                if (success) {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, MainActivity::class.java))
                } else {
                    Toast.makeText(context, "Login Failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
