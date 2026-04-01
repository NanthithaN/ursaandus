package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var userDb: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        // ✅ APPLY ANIMATION TO CARD
        val loginCard = findViewById<LinearLayout>(R.id.loginCard)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up)
        loginCard?.startAnimation(animation)

        userDb = UserDatabaseHelper(this)

        val emailInput = findViewById<EditText>(R.id.etLoginMail)
        val pinInput = findViewById<EditText>(R.id.etLoginPin)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        loginBtn.setOnClickListener {
            val enteredEmail = emailInput.text.toString().trim()
            val enteredPin = pinInput.text.toString().trim()

            if (enteredEmail.isEmpty() || enteredPin.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userData = userDb.validateUser(enteredEmail, enteredPin)

            if (userData != null) {
                sharedPref.edit {
                    putString("username", userData["name"])
                    putString("email", userData["email"])
                    putBoolean("isLoggedIn", true)
                }
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Email or PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
