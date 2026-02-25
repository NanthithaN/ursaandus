package com.example.ursaandus

import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity

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

            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"

            val intent = Intent(this, JournalActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)

            finish()
        }

    }
}
