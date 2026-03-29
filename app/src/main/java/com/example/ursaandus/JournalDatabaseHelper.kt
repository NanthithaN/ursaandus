package com.example.ursaandus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class JournalDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "JournalDB", null, 7) { // Version 7 for multiple media

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE journal (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_email TEXT,
            date TEXT,
            title TEXT,
            content TEXT,
            media_uris TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.execSQL("DROP TABLE IF EXISTS journal")
            onCreate(db)
        }
    }

    fun insertJournal(userEmail: String, date: String, title: String, content: String, mediaUris: String?) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("user_email", userEmail)
        values.put("date", date)
        values.put("title", title)
        values.put("content", content)
        values.put("media_uris", mediaUris)

        db.delete("journal", "user_email=? AND date=?", arrayOf(userEmail, date))
        db.insert("journal", null, values)
    }

    fun getJournalData(userEmail: String, date: String): Map<String, String?>? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT title, content, media_uris FROM journal WHERE user_email=? AND date=?",
            arrayOf(userEmail, date)
        )

        var data: Map<String, String?>? = null
        if (cursor.moveToFirst()) {
            data = mapOf(
                "title" to cursor.getString(0),
                "content" to cursor.getString(1),
                "media_uris" to cursor.getString(2)
            )
        }
        cursor.close()
        return data
    }
}
