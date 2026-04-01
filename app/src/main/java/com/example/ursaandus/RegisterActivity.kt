package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDb: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        // ✅ Make it edge-to-edge for modern look
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_register)

        // ✅ APPLY ANIMATION TO REGISTER CARD
        val registerCard = findViewById<LinearLayout>(R.id.registerCard)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up)
        registerCard?.startAnimation(animation)

        userDb = UserDatabaseHelper(this)

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
                val success = userDb.registerUser(name, mail, pin)
                if (success) {
                    sharedPref.edit {
                        putString("username", name)
                        putString("email", mail)
                        putBoolean("isLoggedIn", true)
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Registration Successful")
                        .setMessage("You have registered successfully, $name!")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
