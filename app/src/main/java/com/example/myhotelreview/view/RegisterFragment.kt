package com.example.myhotelreview.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import android.widget.TextView
import android.widget.Toast
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.RegisterViewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by viewModels()
    private lateinit var navController: NavController
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)
        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val name = etName.text.toString().trim()

            if (password == confirmPassword) {
                registerViewModel.register(email, password, name, requireContext()) { success ->
                    if (success) {
                        // Sign out the user after registration to ensure they go back to login
                        auth.signOut()
                        navController.navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            navController.navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}
