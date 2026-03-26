package com.example.ursaandus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class JournalDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "JournalDB", null, 4) { // Updated version for user-specific journals

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE journal (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_email TEXT,
            date TEXT,
            content TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            // Drop and recreate to add user_email column reliably
            db.execSQL("DROP TABLE IF EXISTS journal")
            onCreate(db)
        }
    }

    fun insertJournal(userEmail: String, date: String, content: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("user_email", userEmail)
        values.put("date", date)
        values.put("content", content)

        // Delete existing entry for this specific user and date before inserting new one
        db.delete("journal", "user_email=? AND date=?", arrayOf(userEmail, date))
        db.insert("journal", null, values)
    }

    fun getJournal(userEmail: String, date: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT content FROM journal WHERE user_email=? AND date=?",
            arrayOf(userEmail, date)
        )

        var content: String? = null
        if (cursor.moveToFirst()) {
            content = cursor.getString(0)
        }
        cursor.close()
        return content
    }
}
