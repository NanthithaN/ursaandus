package com.example.ursaandus

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JournalActivity : AppCompatActivity() {

    private lateinit var journalText: EditText
    private lateinit var dateText: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var saveBtn: Button
    private lateinit var dbHelper: JournalDatabaseHelper
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        dbHelper = JournalDatabaseHelper(this)

        journalText = findViewById(R.id.etJournal)
        dateText = findViewById(R.id.tvSelectedDate)
        backBtn = findViewById(R.id.btnBack)
        saveBtn = findViewById(R.id.btnSave)

        selectedDate = intent.getStringExtra("selectedDate")
            ?: java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

        dateText.text = selectedDate

        // Load journal from SQLite
        val savedText = dbHelper.getJournal(selectedDate!!)
        if (savedText != null) {
            journalText.setText(savedText)
        }

        // ✅ Save button click
        saveBtn.setOnClickListener {
            saveJournal()
        }

        // Back button click
        backBtn.setOnClickListener {
            saveJournal()
            finish()
        }
    }

    private fun saveJournal() {
        val date = selectedDate ?: return
        val content = journalText.text.toString()

        if (content.isNotEmpty()) {
            dbHelper.insertJournal(date, content)
            
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
