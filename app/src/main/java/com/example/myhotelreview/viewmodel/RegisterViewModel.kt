package com.example.myhotelreview.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.model.User
import com.example.myhotelreview.model.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseRepository()
    private val userRepository: UserRepository = UserRepository(application)

    fun register(email: String, password: String, name: String, context: Context, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            callback(false)
        } else if (!isValidEmail(email)) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            callback(false)
        } else if (!isValidPassword(password)) {
            showPasswordGuidelinesDialog(context)
            callback(false)
        } else {
            firebaseRepository.registerUser(email, password, name) { isSuccess, errorMessage ->
                if (isSuccess) {
                    val userId = firebaseRepository.getCurrentUserId()
                    if (userId != null) {
                        val user = User(id = userId, name = name, email = email)
                        // Save user data in Room locally
                        viewModelScope.launch {
                            userRepository.insertUser(user)
                        }
                    }
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                    callback(true)
                } else {
                    Toast.makeText(context, "Registration Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6,}$")
        return passwordPattern.matches(password)
    }

    private fun showPasswordGuidelinesDialog(context: Context) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Password Guidelines")
        builder.setMessage(
            "Your password must meet the following criteria:\n\n" +
                    "- At least 6 characters long\n" +
                    "- Includes at least one uppercase letter\n" +
                    "- Includes at least one lowercase letter\n" +
                    "- Includes at least one number\n" +
                    "- Includes at least one special character (e.g., @, \$, !, %, *, ?, &)"
        )
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
