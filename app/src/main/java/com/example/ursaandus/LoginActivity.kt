package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.etLoginMail)
        val pinInput = findViewById<EditText>(R.id.etLoginPin)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        loginBtn.setOnClickListener {

            val enteredEmail = emailInput.text.toString().trim()
            val enteredPin = pinInput.text.toString().trim()

            val savedEmail = sharedPref.getString("email", "")?.trim()
            val savedPin = sharedPref.getString("pin", "")?.trim()
            val username = sharedPref.getString("username", "")

            if (
                enteredEmail.equals(savedEmail, ignoreCase = true) &&
                enteredPin == savedPin
            ) {

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish()

            } else {

                Toast.makeText(this, "Invalid Email or PIN", Toast.LENGTH_SHORT).show()
            }

        }
    }
}