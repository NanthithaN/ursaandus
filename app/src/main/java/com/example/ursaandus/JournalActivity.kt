package com.example.ursaandus

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class JournalActivity : AppCompatActivity() {

    private lateinit var journalText: EditText
    private lateinit var dateText: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var saveBtn: Button
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        journalText = findViewById(R.id.etJournal)
        dateText = findViewById(R.id.tvSelectedDate)
        backBtn = findViewById(R.id.btnBack)
        saveBtn = findViewById(R.id.btnSave)

        selectedDate = intent.getStringExtra("selectedDate")
            ?: java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())


        if (selectedDate != null) {
            dateText.text = selectedDate

            val sharedPref = getSharedPreferences("JournalData", MODE_PRIVATE)
            val savedText = sharedPref.getString(selectedDate, "")
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

        if (selectedDate == null) return

        val sharedPref = getSharedPreferences("JournalData", MODE_PRIVATE)

        sharedPref.edit {
            putString(selectedDate, journalText.text.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Saved ✨")
            .setMessage("Journal saved for $selectedDate 💛")
            .setPositiveButton("OK", null)
            .show()
    }

}
