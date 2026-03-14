package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameEditText = findViewById<EditText>(R.id.etName)
        val mailEditText = findViewById<EditText>(R.id.etMail)
        val pinEditText = findViewById<EditText>(R.id.etPin)
        val registerBtn = findViewById<Button>(R.id.btnRegister)
        val loginLink = findViewById<TextView>(R.id.tvLoginLink)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        registerBtn.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val mail = mailEditText.text.toString().trim()
            val pin = pinEditText.text.toString().trim()

            if (name.isNotEmpty() && mail.isNotEmpty() && pin.isNotEmpty()) {
                // Save user session
                sharedPref.edit {
                    putString("username", name)
                    putString("email", mail)
                    putString("pin", pin)
                    putBoolean("isLoggedIn", true)
                }

                // Show success dialog
                AlertDialog.Builder(this)
                    .setTitle("Registration Successful")
                    .setMessage("You have registered successfully, $name!")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("username", name)
                        startActivity(intent)
                        finish()
                    }
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please fill in all fields")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
