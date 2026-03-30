package com.example.ursaandus

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class JournalActivity : AppCompatActivity() {

    private lateinit var journalText: EditText
    private lateinit var etTitle: EditText
    private lateinit var dateText: TextView
    private lateinit var saveBtn: Button
    private lateinit var backBtn: ImageButton
    private lateinit var mediaContainer: LinearLayout
    private lateinit var smsBtn: Button
    private lateinit var whatsappBtn: Button

    private lateinit var dbHelper: JournalDatabaseHelper
    private var selectedDate: String? = null
    private val mediaUris = mutableListOf<String>()

    private val database = FirebaseDatabase.getInstance("https://ursaus-cc729-default-rtdb.firebaseio.com/")
    private val journalRef = database.getReference("journals")

    private val pickMedia = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addMediaToView(uri.toString())
                mediaUris.add(uri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        dbHelper = JournalDatabaseHelper(this)

        journalText = findViewById(R.id.etJournal)
        etTitle = findViewById(R.id.etTitle)
        dateText = findViewById(R.id.tvSelectedDate)
        saveBtn = findViewById(R.id.btnSave)
        backBtn = findViewById(R.id.btnBack)
        mediaContainer = findViewById(R.id.mediaContainer)

        val btnImage = findViewById<Button>(R.id.btnAddImage)
        val btnVideo = findViewById<Button>(R.id.btnAddVideo)

        selectedDate = intent.getStringExtra("selectedDate")
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("username", "User")
        val userEmail = sharedPref.getString("email", "guest")

        if (selectedDate != null) {
            dateText.text = selectedDate

            // 1. Load from SQLite
            val savedData = dbHelper.getJournalData(userEmail!!, selectedDate!!)
            if (savedData != null) {
                etTitle.setText(savedData["title"])
                journalText.setText(savedData["content"])
                
                savedData["media_uris"]?.let { uris ->
                    if (uris.isNotEmpty()) {
                        uris.split(",").forEach { uriStr ->
                            addMediaToView(uriStr)
                            mediaUris.add(uriStr)
                        }
                    }
                }
            }

            // 2. Fetch from Firebase
            fetchFromFirebase(username, userEmail, selectedDate!!)
        }

        btnImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickMedia.launch(intent)
        }

        btnVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            pickMedia.launch(intent)
        }

        saveBtn.setOnClickListener { saveJournal(username, userEmail!!) }
        backBtn.setOnClickListener { finish() }
        smsBtn = findViewById(R.id.btnSMS)
        whatsappBtn = findViewById(R.id.btnWhatsApp)

        // 📩 SMS
        smsBtn.setOnClickListener {
            val message = journalText.text.toString()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("sms:")
            intent.putExtra("sms_body", message)
            startActivity(intent)
        }

        // 📤 WhatsApp
        whatsappBtn.setOnClickListener {
            val message = journalText.text.toString()
            val intent = Intent(Intent.ACTION_SEND)
            
            // Fix: mediaUri was undefined, using the first one from the list if available
            val firstMediaUri = mediaUris.firstOrNull()?.let { Uri.parse(it) }

            if (firstMediaUri != null) {
                intent.type = "image/*"   // works for both image & video safely
                intent.putExtra(Intent.EXTRA_STREAM, firstMediaUri)
                intent.putExtra(Intent.EXTRA_TEXT, message)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, message)
            }

            intent.setPackage("com.whatsapp")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    } // ✅ Correctly closing onCreate

    private fun addMediaToView(uriStr: String) {
        val uri = Uri.parse(uriStr)
        val type = contentResolver.getType(uri)

        if (type?.startsWith("image") == true) {
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500
            ).apply { setMargins(0, 10, 0, 10) }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(uri)
            mediaContainer.addView(imageView)
        } else if (type?.startsWith("video") == true) {
            val videoView = VideoView(this)
            videoView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500
            ).apply { setMargins(0, 10, 0, 10) }
            videoView.setVideoURI(uri)
            mediaContainer.addView(videoView)
            videoView.start()
        }
    }

    private fun fetchFromFirebase(username: String?, userEmail: String, date: String) {
        if (username == null) return
        val safeDateKey = date.replace(" ", "_")
        journalRef.child(username).child(safeDateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val title = snapshot.child("title").value?.toString() ?: ""
                val content = snapshot.child("content").value?.toString() ?: ""
                val uris = snapshot.child("media_uris").value?.toString() ?: ""
                
                if (etTitle.text.isEmpty()) {
                    etTitle.setText(title)
                    journalText.setText(content)
                    if (uris.isNotEmpty()) {
                        mediaContainer.removeAllViews()
                        mediaUris.clear()
                        uris.split(",").forEach {
                            addMediaToView(it)
                            mediaUris.add(it)
                        }
                    }
                }
                dbHelper.insertJournal(userEmail, date, title, content, uris)
            }
        }
    }

    private fun saveJournal(username: String?, userEmail: String) {
        val title = etTitle.text.toString()
        val content = journalText.text.toString()
        val date = selectedDate ?: return
        val uris = mediaUris.joinToString(",")

        dbHelper.insertJournal(userEmail, date, title, content, uris)

        if (username != null) {
            val safeDateKey = date.replace(" ", "_")
            val data = mapOf(
                "title" to title,
                "content" to content,
                "media_uris" to uris
            )
            journalRef.child(username).child(safeDateKey).setValue(data)
        }

        Toast.makeText(this, "Saved ✨", Toast.LENGTH_SHORT).show()
    }
}
