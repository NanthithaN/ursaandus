package com.example.ursaandus

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class HomeActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var receiver: NetworkReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        val welcomeText = findViewById<TextView>(R.id.tvGreeting)
        val brewBtn = findViewById<Button>(R.id.btnBrew)
        val calendarBtn = findViewById<Button>(R.id.btnCalendar)
        val locationBtn = findViewById<Button>(R.id.btnLocation)
        val logoutBtn = findViewById<Button>(R.id.btnLogout)

        val username = sharedPref.getString("username", "Rose")
        welcomeText.text = "Hi $username! What's brewing in your honey pot of thoughts?"

        brewBtn.setOnClickListener {
            val currentDate = java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            val intent = Intent(this, JournalActivity::class.java)
            intent.putExtra("selectedDate", currentDate)
            startActivity(intent)
        }

        calendarBtn.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        locationBtn.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            logout()
        }
        receiver = NetworkReceiver()
        registerReceiver(receiver, android.content.IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logout() {
        sharedPref.edit { 
            putBoolean("isLoggedIn", false) 
        }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
