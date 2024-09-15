package com.example.myhotelreview.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.myhotelreview.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setupWithNavController(navController)

        //Hiding the bottom menu from the login and register pages
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> bottomNavigationView.visibility = View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_hotels -> {
                    navController.navigate(R.id.hotelsFragment)
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.nav_my_comments -> {
                    navController.navigate(R.id.myCommentsFragment)
                    true
                }
                R.id.nav_logout -> {
                    auth.signOut()
                    navController.navigate(R.id.loginFragment)
                    true
                }
                else -> false
            }
        }

        if (auth.currentUser == null) {
            navController.navigate(R.id.loginFragment)
        } else {
            navController.navigate(R.id.hotelsFragment)
        }
    }
}
