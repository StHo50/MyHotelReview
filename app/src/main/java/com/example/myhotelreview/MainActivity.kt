package com.example.myhotelreview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            database.child(userId).get().addOnSuccessListener {
                if (it.exists()) {
                    val email = it.child("email").value.toString()
                    tvWelcome.text = "Welcome, $email"
                } else {
                    tvWelcome.text = "Welcome!"
                }
            }.addOnFailureListener {
                tvWelcome.text = "Welcome!"
            }
        } else {
            tvWelcome.text = "Welcome!"
        }
    }
}
