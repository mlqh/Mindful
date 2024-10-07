package com.ece452s24g7.mindful.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.activities.viewmodels.CreateEntryViewModel
import com.ece452s24g7.mindful.adapters.PhotoVideoListAdapter
import com.ece452s24g7.mindful.views.AudioPlayerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateEntryActivity : AppCompatActivity() {
    private val viewModel by viewModels<CreateEntryViewModel>()

    private lateinit var dateTextView: TextView
    private lateinit var bodyEditText: EditText
    private lateinit var selectedPhotoView: RecyclerView
    private lateinit var selectedVideoView: RecyclerView
    private lateinit var saveButton: Button
    private lateinit var recordAudioButton: Button
    private lateinit var audioPlayer: AudioPlayerView
    private lateinit var addPhotoButton: Button
    private lateinit var addVideoButton: Button
    private lateinit var locationTextView: TextView

    private var id: Int = -1
    private var entryDate: Date = Date(0L)
    private var entryImageURIs: List<String> = emptyList()
    private var entryVideoURIs: List<String> = emptyList()
    private var entryAudioPath: String? = null
    private var currentLocation: String? = null
    private lateinit var locationManager: LocationManager

    private val photoRequestCode = 1
    private val videoRequestCode = 2
    private val audioRequestCode = 3
    private val locationRequestCode = 4

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_entry)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_entry)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val entryId = intent.getIntExtra("entry_id", -1)
        id = entryId

        dateTextView = findViewById(R.id.editor_date_text_view)
        val dateLong = intent.getLongExtra("entry_date", -1L)
        entryDate = if (dateLong == -1L) {
            Date()
        } else {
            Date(dateLong)
        }
        val sdf = SimpleDateFormat("EEEE MMMM d yyyy, h:mm a")
        dateTextView.text = sdf.format(entryDate)

        bodyEditText = findViewById(R.id.editor_body_edit_text)
        val bodyString = intent.getStringExtra("entry_body")
        bodyEditText.setText(bodyString)

        selectedPhotoView = findViewById(R.id.editor_selected_photo_view)
        selectedPhotoView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        val imageURIs = intent.getStringArrayExtra("entry_imageURIs")
        if (imageURIs != null) {
            entryImageURIs = imageURIs.toList()
        }
        selectedPhotoView.adapter = PhotoVideoListAdapter(entryImageURIs, emptyList())

        selectedVideoView = findViewById(R.id.editor_selected_video_view)
        selectedVideoView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        val videoURIs = intent.getStringArrayExtra("entry_videoURIs")
        if (videoURIs != null) {
            entryVideoURIs = videoURIs.toList()
        }
        selectedVideoView.adapter = PhotoVideoListAdapter(emptyList(), entryVideoURIs)

        audioPlayer = findViewById(R.id.editor_audio_player)
        val audioPath = intent.getStringExtra("entry_audioPath")
        entryAudioPath = audioPath
        audioPlayer.setFilePath(audioPath)

        locationTextView = findViewById(R.id.editor_location_text_view)
        val location = intent.getStringExtra("entry_location")
        if (location != null) {
            currentLocation = location
            locationTextView.text = currentLocation
            locationTextView.visibility = View.VISIBLE
        } else {
            // Initialize location manager and request location updates
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            attemptLocationRequest()
        }

        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveEntry()
            finish()
        }

        recordAudioButton = findViewById(R.id.editor_audio_record_button)
        recordAudioButton.setOnClickListener {
            val audioRecorderIntent = Intent(this, AudioRecorderActivity::class.java)
            startActivityForResult(audioRecorderIntent, audioRequestCode)
        }

        addPhotoButton = findViewById(R.id.editor_add_photo_button)
        addPhotoButton.setOnClickListener {
            if (photoPermGranted()) {
                openPhotoGallery()
            }
        }

        addVideoButton = findViewById(R.id.editor_add_video_button)
        addVideoButton.setOnClickListener {
            if (videoPermGranted()) {
                openVideoGallery()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                photoRequestCode -> openPhotoGallery()
                videoRequestCode -> openVideoGallery()
                locationRequestCode -> attemptLocationRequest()
            }
        }
    }

    private fun openPhotoGallery() {
        val openPhotoGalleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(openPhotoGalleryIntent, "Select Pictures"), photoRequestCode)
    }

    private fun openVideoGallery() {
        val openVideoGalleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(openVideoGalleryIntent, "Select Videos"), videoRequestCode)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == audioRequestCode) {
            // audio
            entryAudioPath = data?.getStringExtra("entry_audioPath")
            audioPlayer.setFilePath(entryAudioPath)
        } else {
            // photo/video
            val mutableMediaURIs = mutableListOf<String>()
            if (data?.clipData != null) {
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, flag)
                    mutableMediaURIs.add(uri.toString())
                }
            }
            val mediaURIs = mutableMediaURIs.toList()
            when (requestCode) {
                photoRequestCode -> {
                    entryImageURIs = mediaURIs
                    selectedPhotoView.adapter = PhotoVideoListAdapter(entryImageURIs, emptyList())
                }
                videoRequestCode -> {
                    entryVideoURIs = mediaURIs
                    selectedVideoView.adapter = PhotoVideoListAdapter(emptyList(), entryVideoURIs)
                }
            }
        }
    }

    private fun saveEntry() {
        val currentBody = bodyEditText.text.toString()
        viewModel.saveEntry(
            id,
            entryDate,
            currentBody,
            entryImageURIs,
            entryVideoURIs,
            entryAudioPath,
            currentLocation
        )
    }

    private fun photoPermGranted(): Boolean {
        val perm: String = if (Build.VERSION.SDK_INT <= 32) {
            Manifest.permission.READ_EXTERNAL_STORAGE
        } else {
            Manifest.permission.READ_MEDIA_IMAGES
        }
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), photoRequestCode)
            return false
        }
        return true
    }

    private fun videoPermGranted(): Boolean {
        val perm: String = if (Build.VERSION.SDK_INT <= 32) {
            Manifest.permission.READ_EXTERNAL_STORAGE
        } else {
            Manifest.permission.READ_MEDIA_VIDEO
        }
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), videoRequestCode)
            return false
        }
        return true
    }

    private fun attemptLocationRequest() {
        val perm = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), locationRequestCode)
        } else {
            val provider = if (Build.VERSION.SDK_INT <= 30) {
                LocationManager.NETWORK_PROVIDER
            } else {
                LocationManager.FUSED_PROVIDER
            }
            val geocoder = Geocoder(this, Locale.getDefault())
            locationManager.requestLocationUpdates(provider, 1000L, 0f, object: LocationListener {
                override fun onLocationChanged(location: Location) {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            currentLocation = if (address.featureName in address.getAddressLine(0)) {
                                "${address.locality}, ${address.adminArea}"
                            } else {
                                address.featureName
                            }
                            locationTextView.text = currentLocation
                            locationTextView.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            })
        }
    }
}