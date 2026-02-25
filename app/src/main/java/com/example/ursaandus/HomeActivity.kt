package com.example.ursaandus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
private val REQUEST_NOTIFICATION_PERMISSION = 1

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username")
        val welcomeText = findViewById<TextView>(R.id.tvGreeting)

        welcomeText.text =
            "Hi $username! What's brewing in your honey pot of thoughts?"

        // Context menu
        registerForContextMenu(welcomeText)

        val brewBtn = findViewById<Button>(R.id.btnBrew)
        val calendarBtn = findViewById<Button>(R.id.btnCalendar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Example progress value
        progressBar.progress = 70

        // Brew Button Click
        brewBtn.setOnClickListener {

            val currentDate = java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            val intent = Intent(this, JournalActivity::class.java)
            intent.putExtra("selectedDate", currentDate)
            startActivity(intent)
        }


        // Calendar Button Click
        calendarBtn.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        // ✅ Create Notification Channel (Required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "journal_channel",
                "Journal Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
        val locationBtn = findViewById<Button>(R.id.btnLocation)

        locationBtn.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

    }

    // 🔔 Notification Function
    private fun showNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val builder = NotificationCompat.Builder(this, "journal_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Journal Saved 🌸")
            .setContentText("Your thoughts are safely stored!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(1, builder.build())
    }


    // ✅ Context Menu
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.home_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_logout -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    // ✅ Options Menu (3 dots)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.opt_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.opt_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.opt_logout -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
