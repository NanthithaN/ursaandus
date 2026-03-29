package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var userDb: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // ✅ HIDE HEADER
        setContentView(R.layout.activity_login)

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

            // ✅ VALIDATE AGAINST DATABASE
            val userData = userDb.validateUser(enteredEmail, enteredPin)

            if (userData != null) {
                // Successful login, update session
                sharedPref.edit {
                    putString("username", userData["name"])
                    putString("email", userData["email"])
                    putBoolean("isLoggedIn", true)
                }

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Invalid Email or PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }
}