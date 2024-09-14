package com.example.myhotelreview.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.myhotelreview.model.FirebaseRepository

class RegisterViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    fun register(email: String, password: String, name: String, context: Context, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            callback(false)
        } else if (!isValidPassword(password)) {
            showPasswordGuidelinesDialog(context)
            callback(false)
        } else {
            firebaseRepository.registerUser(email, password, name) { isSuccess, errorMessage ->
                if (isSuccess) {
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                    callback(true) // Success callback
                } else {
                    Toast.makeText(context, "Registration Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    callback(false) // Failure callback
                }
            }
        }
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
