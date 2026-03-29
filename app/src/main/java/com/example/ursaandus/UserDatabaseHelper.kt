package com.example.ursaandus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "UserDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            email TEXT UNIQUE,
            pin TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun registerUser(name: String, email: String, pin: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("pin", pin)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun validateUser(email: String, pin: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name, email FROM users WHERE email=? AND pin=?", arrayOf(email, pin))
        
        var userData: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            userData = mapOf(
                "name" to cursor.getString(0),
                "email" to cursor.getString(1)
            )
        }
        cursor.close()
        return userData
    }
}
