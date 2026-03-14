package com.example.ursaandus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class JournalDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "JournalDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE journal (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT,
            content TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS journal")
        onCreate(db)
    }

    fun insertJournal(date: String, content: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("date", date)
        values.put("content", content)

        // Using REPLACE to update if entry for the date already exists
        db.insertWithOnConflict("journal", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getJournal(date: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT content FROM journal WHERE date=?",
            arrayOf(date)
        )

        var content: String? = null
        if (cursor.moveToFirst()) {
            content = cursor.getString(0)
        }
        cursor.close()
        return content
    }
}
