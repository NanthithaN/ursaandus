package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val datePicker = findViewById<DatePicker>(R.id.datePicker)

        datePicker.init(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth
        ) { _, year, monthOfYear, dayOfMonth ->

            // Create a Calendar instance to format the date correctly
            val calendar = Calendar.getInstance()
            calendar.set(year, monthOfYear, dayOfMonth)

            // Use the SAME format as HomeActivity: "dd MMM yyyy"
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val selectedDate = sdf.format(calendar.time)

            val intent = Intent(this, JournalActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)

            finish()
        }
    }
}
