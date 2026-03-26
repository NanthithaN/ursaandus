package com.example.ursaandus

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class JournalActivity : AppCompatActivity() {

    private lateinit var journalText: EditText
    private lateinit var dateText: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var saveBtn: Button
    private lateinit var dbHelper: JournalDatabaseHelper
    private var selectedDate: String? = null

    // Explicitly using your Database URL
    private val database = FirebaseDatabase.getInstance("https://ursaus-cc729-default-rtdb.firebaseio.com/")
    private val journalRef = database.getReference("journals")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        dbHelper = JournalDatabaseHelper(this)

        journalText = findViewById(R.id.etJournal)
        dateText = findViewById(R.id.tvSelectedDate)
        backBtn = findViewById(R.id.btnBack)
        saveBtn = findViewById(R.id.btnSave)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("username", "User")
        val userEmail = sharedPref.getString("email", "") ?: ""

        selectedDate = intent.getStringExtra("selectedDate")
            ?: java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

        dateText.text = selectedDate

        // Load journal from SQLite (offline-first) - Fixed to use userEmail
        val savedText = dbHelper.getJournal(userEmail, selectedDate!!)
        if (savedText != null) {
            journalText.setText(savedText)
        }

        // Also fetch from Firebase to sync
        fetchFromFirebase(username, userEmail, selectedDate!!)

        // ✅ Save button click
        saveBtn.setOnClickListener {
            saveJournal(username, userEmail)
        }

        // Back button click
        backBtn.setOnClickListener {
            saveJournal(username, userEmail)
            finish()
        }
    }

    private fun fetchFromFirebase(username: String?, userEmail: String, date: String) {
        if (username == null) return

        val safeDateKey = date.replace(" ", "_")

        journalRef.child(username).child(safeDateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val content = snapshot.child("content").value.toString()
                journalText.setText(content)
                // Also update local SQLite to keep them in sync
                dbHelper.insertJournal(userEmail, date, content)
            }
        }.addOnFailureListener { e ->
            // Logging failure to fetch
            android.util.Log.e("FirebaseSync", "Error fetching data", e)
        }
    }

    private fun saveJournal(username: String?, userEmail: String) {
        val date = selectedDate ?: return
        val content = journalText.text.toString()

        if (content.isNotEmpty()) {
            // 1. Save to SQLite (for offline access)
            dbHelper.insertJournal(userEmail, date, content)
            
            // 2. Save to Firebase
            if (username != null) {
                val safeDateKey = date.replace(" ", "_")
                val journalData = mapOf(
                    "date" to date,
                    "content" to content,
                    "timestamp" to System.currentTimeMillis()
                )

                journalRef.child(username).child(safeDateKey).setValue(journalData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Synced with Cloud ✨", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to sync: ${e.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("FirebaseSync", "Error saving data", e)
                    }
            }

            AlertDialog.Builder(this)
                .setTitle("Saved ✨")
                .setMessage("Journal saved for $date 💛")
                .setPositiveButton("OK", null)
                .show()
        } else {
            Toast.makeText(this, "Journal is empty", Toast.LENGTH_SHORT).show()
        }
    }
}
