package com.example.ursaandus

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class JournalActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var journalText: EditText
    private lateinit var etTitle: EditText
    private lateinit var dateText: TextView
    private lateinit var tvTopQuote: TextView
    private lateinit var saveBtn: Button
    private lateinit var backBtn: ImageButton
    private lateinit var mediaContainer: LinearLayout
    private lateinit var vibeContainer: LinearLayout
    private lateinit var journalBox: LinearLayout
    private lateinit var journalRoot: ConstraintLayout
    private lateinit var bgImage: ImageView
    
    private lateinit var dbHelper: JournalDatabaseHelper
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    
    private var selectedDate: String? = null
    private val mediaUris = mutableListOf<String>()
    private var currentPhotoPath: String? = null
    private var currentVibes = mutableSetOf<String>()

    private val database = FirebaseDatabase.getInstance("https://ursaus-cc729-default-rtdb.firebaseio.com/")
    private val journalRef = database.getReference("journals")

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        if (results.values.all { it }) {
            captureEnvironmentVibes()
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addMediaToView(uri.toString())
                    mediaUris.add(uri.toString())
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val file = File(currentPhotoPath!!)
            val uri = FileProvider.getUriForFile(this, "com.example.ursaandus.fileprovider", file)
            addMediaToView(uri.toString())
            mediaUris.add(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_journal)

        dbHelper = JournalDatabaseHelper(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        journalRoot = findViewById(R.id.journalRoot)
        bgImage = findViewById(R.id.bgImage)
        journalText = findViewById(R.id.etJournal)
        etTitle = findViewById(R.id.etTitle)
        dateText = findViewById(R.id.tvSelectedDate)
        tvTopQuote = findViewById(R.id.tvTopQuote)
        saveBtn = findViewById(R.id.btnSave)
        backBtn = findViewById(R.id.btnBack)
        mediaContainer = findViewById(R.id.mediaContainer)
        journalBox = findViewById(R.id.journalBox)
        
        vibeContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        (mediaContainer.parent as ViewGroup).addView(vibeContainer, 0)

        val btnImage = findViewById<Button>(R.id.btnAddImage)
        val btnVideo = findViewById<Button>(R.id.btnAddVideo)

        selectedDate = intent.getStringExtra("selectedDate")
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("username", "User")
        val userEmail = sharedPref.getString("email", "guest")

        dateText.text = selectedDate

        val savedData = dbHelper.getJournalData(userEmail!!, selectedDate!!)
        if (savedData != null) {
            etTitle.setText(savedData["title"])
            journalText.setText(savedData["content"])
            savedData["media_uris"]?.split(",")?.filter { it.isNotEmpty() }?.forEach {
                addMediaToView(it)
                mediaUris.add(it)
            }
        }

        btnImage.setOnClickListener { showImageSourceDialog() }
        btnVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            pickMedia.launch(intent)
        }

        saveBtn.setOnClickListener { saveJournal(username, userEmail!!) }
        backBtn.setOnClickListener { finish() }
        
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.CAMERA)
        requestPermissions.launch(permissions.toTypedArray())
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Gallery", "Camera")
        AlertDialog.Builder(this)
            .setTitle("Add Image From")
            .setItems(options) { _, which ->
                if (which == 0) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    pickMedia.launch(intent)
                } else {
                    openCamera()
                }
            }.show()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        currentPhotoPath = photoFile.absolutePath
        val photoURI = FileProvider.getUriForFile(this, "com.example.ursaandus.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        takePhoto.launch(intent)
    }

    private fun captureEnvironmentVibes() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) updateVibe("🏠", true)

        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (btManager.adapter?.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED) {
                updateVibe("🎧", true)
            }
        }
    }

    private fun updateVibe(emoji: String, add: Boolean) {
        if (add) {
            if (!currentVibes.contains(emoji)) {
                currentVibes.add(emoji)
                val tv = TextView(this).apply {
                    tag = emoji
                    text = emoji
                    textSize = 24f
                    setPadding(10, 0, 10, 0)
                }
                vibeContainer.addView(tv)
            }
        } else {
            currentVibes.remove(emoji)
            val view = vibeContainer.findViewWithTag<View>(emoji)
            if (view != null) vibeContainer.removeView(view)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            if (lux < 10) {
                updateVibe("🌙", true)
                applyLocalDarkTheme(true)
            } else {
                updateVibe("🌙", false)
                applyLocalDarkTheme(false)
            }
        }
    }

    private fun applyLocalDarkTheme(isDark: Boolean) {
        if (isDark) {
            // ✅ COMPLETE DARK THEME
            journalRoot.setBackgroundColor(Color.BLACK)
            bgImage.visibility = View.GONE
            
            // ✅ USE ROUNDED GREY DRAWABLE
            journalBox.setBackgroundResource(R.drawable.rounded_grey_box)
            etTitle.setTextColor(Color.BLACK)
            journalText.setTextColor(Color.BLACK)
            dateText.setTextColor(Color.BLACK)
            etTitle.setHintTextColor(Color.GRAY)
            journalText.setHintTextColor(Color.GRAY)
            
            // Text outside the box
            tvTopQuote.setTextColor(Color.WHITE)
        } else {
            // ✅ BRIGHT THEME RESTORE
            journalRoot.setBackgroundColor(Color.TRANSPARENT)
            bgImage.visibility = View.VISIBLE
            
            journalBox.setBackgroundResource(R.drawable.rounded_transparent_box)
            etTitle.setTextColor(Color.parseColor("#5C3A2E"))
            journalText.setTextColor(Color.parseColor("#5C3A2E"))
            dateText.setTextColor(Color.parseColor("#5C3A2E"))
            tvTopQuote.setTextColor(Color.parseColor("#5C3A2E"))
            etTitle.setHintTextColor(Color.GRAY)
            journalText.setHintTextColor(Color.GRAY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        captureEnvironmentVibes()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun addMediaToView(uriStr: String) {
        val uri = Uri.parse(uriStr)
        val type = contentResolver.getType(uri) ?: if (uriStr.contains("IMG")) "image/jpeg" else "video/mp4"

        if (type.startsWith("image")) {
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500).apply { setMargins(0, 10, 0, 10) }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }
            mediaContainer.addView(iv)
        } else {
            val vv = VideoView(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500).apply { setMargins(0, 10, 0, 10) }
                setVideoURI(uri)
            }
            mediaContainer.addView(vv)
            vv.start()
        }
    }

    private fun saveJournal(username: String?, userEmail: String) {
        val title = etTitle.text.toString()
        val vibeText = currentVibes.joinToString(" ")
        val content = journalText.text.toString() + "\n[Vibes: $vibeText]"
        val date = selectedDate ?: return
        val uris = mediaUris.joinToString(",")

        dbHelper.insertJournal(userEmail, date, title, content, uris)

        if (username != null) {
            val safeDateKey = date.replace(" ", "_")
            journalRef.child(username).child(safeDateKey).setValue(mapOf(
                "title" to title, "content" to content, "media_uris" to uris
            ))
        }
        Toast.makeText(this, "Saved ✨", Toast.LENGTH_SHORT).show()
    }
}
