package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerBtn = findViewById<Button>(R.id.btnRegister)

        registerBtn.setOnClickListener {

            val name = findViewById<EditText>(R.id.etName).text.toString()
            val mail = findViewById<EditText>(R.id.etMail).text.toString()
            val pin = findViewById<EditText>(R.id.etPin).text.toString()

            // ⭐ POPUP DIALOG
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Registration Successful")
            builder.setMessage("You have registered successfully!")
            builder.setCancelable(false)

            builder.setPositiveButton("OK") { _, _ ->
                // ⭐ NAVIGATION MOVED INSIDE POPUP
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("username", name)
                startActivity(intent)
                finish()
            }

            builder.show()
        }
    }
}
