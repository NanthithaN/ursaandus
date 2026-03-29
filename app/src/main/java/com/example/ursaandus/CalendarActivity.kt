package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var dbHelper: JournalDatabaseHelper
    private lateinit var previewLayout: LinearLayout
    private lateinit var tvNoJournal: TextView
    private lateinit var tvTitlePreview: TextView
    private lateinit var tvContentPreview: TextView
    private lateinit var btnOpen: Button
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        dbHelper = JournalDatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userEmail = sharedPref.getString("email", "") ?: ""

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        previewLayout = findViewById(R.id.previewLayout)
        tvNoJournal = findViewById(R.id.tvNoJournal)
        tvTitlePreview = findViewById(R.id.tvTitlePreview)
        tvContentPreview = findViewById(R.id.tvContentPreview)
        btnOpen = findViewById(R.id.btnOpenJournal)

        // Initial check for current date
        checkJournal(datePicker.year, datePicker.month, datePicker.dayOfMonth)

        datePicker.init(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth
        ) { _, year, month, day ->
            checkJournal(year, month, day)
        }

        btnOpen.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val selectedDate = sdf.format(calendar.time)

            val intent = Intent(this, JournalActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }
    }

    private fun checkJournal(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = sdf.format(calendar.time)

        val data = dbHelper.getJournalData(userEmail, dateString)
        if (data != null) {
            tvNoJournal.visibility = View.GONE
            previewLayout.visibility = View.VISIBLE
            
            val title = data["title"]
            tvTitlePreview.text = if (title.isNullOrEmpty()) "😊 Untitled Journal" else "😊 $title"
            
            val content = data["content"] ?: ""
            tvContentPreview.text = if (content.length > 60) content.substring(0, 60) + "..." else content
            
            btnOpen.text = "VIEW FULL JOURNAL"
        } else {
            previewLayout.visibility = View.GONE
            tvNoJournal.visibility = View.VISIBLE
            btnOpen.text = "CREATE NEW JOURNAL"
        }
    }
}
