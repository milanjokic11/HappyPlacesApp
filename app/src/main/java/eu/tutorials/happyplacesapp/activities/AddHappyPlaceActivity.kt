package eu.tutorials.happyplacesapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.database.DatabaseHandler
import eu.tutorials.happyplacesapp.models.HappyPlaceModel
import eu.tutorials.happyplacesapp.utils.GetAddressFromLatLng
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var addPlaceToolbar: Toolbar? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var etDate: EditText? = null
    private var tvAddImage: TextView? = null
    private var ivPlaceImage: ImageView? = null
    private var btnSave: Button? = null
    private var etTitle: EditText? = null
    private var etDescription: EditText? = null
    private var etLocation: EditText? = null
    private var tvSelectCurrentLocation: TextView? = null
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mHappyPlaceDetails: HappyPlaceModel? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        addPlaceToolbar = findViewById(R.id.add_place_toolbar)

        setSupportActionBar(addPlaceToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addPlaceToolbar!!.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@AddHappyPlaceActivity)

        if (!Places.isInitialized()) {
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.maps_api_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }
        updateDateView()
        // EditText for adding date
        etDate = findViewById<EditText>(R.id.et_date)
        etDate?.setOnClickListener(this)
        // TextView for adding location image
        tvAddImage = findViewById<TextView>(R.id.tv_add_image)
        tvAddImage?.setOnClickListener(this)
        // EditText for adding title
        etTitle = findViewById<EditText>(R.id.et_title)
        etDescription = findViewById<EditText>(R.id.et_description)
        // EditText for adding location
        etLocation = findViewById<EditText>(R.id.et_location)
        etLocation?.setOnClickListener(this)
        // Btn for saving happy place info
        btnSave = findViewById<Button>(R.id.btn_save)
        btnSave?.setOnClickListener(this)
        // TextView for setting location to current
        tvSelectCurrentLocation = findViewById(R.id.tv_select_current_location)
        tvSelectCurrentLocation?.setOnClickListener(this)
        // setting up up ImageView for real location image
        ivPlaceImage = findViewById<ImageView>(R.id.iv_place_image)
        // updating happy place entry
        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"
            etTitle?.setText(mHappyPlaceDetails!!.title)
            etDescription?.setText(mHappyPlaceDetails!!.description)
            etDate?.setText(mHappyPlaceDetails!!.date)
            etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude
            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            ivPlaceImage?.setImageURI(saveImageToInternalStorage)
            btnSave?.text = "UPDATE"
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

    }

    private val mLocationCallback = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val mLastLocation: Location? = result.lastLocation
            mLatitude = mLastLocation!!.latitude
            Log.i("Current Latitude:", "$mLatitude")
            mLongitude = mLastLocation!!.longitude
            Log.i("Current Longitude:", "$mLongitude")
            Toast.makeText(this@AddHappyPlaceActivity, "mLocationCallback success", Toast.LENGTH_SHORT).show()

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String) {
                    etLocation?.setText(address)
                }
                override fun onError() {
                    Log.e("Get Address:", "Something went wrong...")
                }
            })
            addressTask.getAddress()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlaceActivity, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image -> {
                val picDialog = AlertDialog.Builder(this)
                picDialog.setTitle("Select Action")
                val picDialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
                picDialog.setItems(picDialogItems) {
                    _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                picDialog.show()
            }
            R.id.btn_save -> {
                when {
                    etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a title...", Toast.LENGTH_SHORT).show()
                    }
                    etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a description...", Toast.LENGTH_SHORT).show()
                    }
                    etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a location...", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please select an image...", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if(mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription?.text.toString(),
                            etDate?.text.toString(),
                            etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)

                        if (mHappyPlaceDetails == null) {
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                            if (addHappyPlace > 0) {
                                Toast.makeText(this@AddHappyPlaceActivity, "The happy place has been successfully inserted...", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                            if (updateHappyPlace > 0) {
                                Toast.makeText(this@AddHappyPlaceActivity, "The happy place has been successfully updated...", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
            R.id.et_location -> {
                try {
                     val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(this@AddHappyPlaceActivity, "Your location provider is turned off, please turn it on to use feature...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this)
                        .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        .withListener(object: MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {
                                    Toast.makeText(this@AddHappyPlaceActivity, "Permission has been granted, you can now access the current location...", Toast.LENGTH_SHORT).show()
                                    requestNewLocationData()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                                showRationaleDialogForPermissions()
                            }
                        }).onSameThread().check()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitMap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitMap)
                        ivPlaceImage?.setImageBitmap(selectedImageBitMap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity, "Failed to load image from Gallery...", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                ivPlaceImage?.setImageBitmap(thumbnail)
            } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                etLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        Toast.makeText(this@AddHappyPlaceActivity, "Storage READ/WRITE permission are granted...", Toast.LENGTH_SHORT).show()
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken ) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        Toast.makeText(this@AddHappyPlaceActivity, "Storage READ/WRITE permission are granted...", Toast.LENGTH_SHORT).show()
                        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken ) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("It seems you have turned off the permissions required for this feature... It can be enabled under Application Settings...")
            .setPositiveButton("GO TO SETTINGS") {
                _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
            }.setNegativeButton("Cancel") {
                dialog, which ->
                    dialog.dismiss()
            }.show()
    }

    private fun updateDateView() {
        val format = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${ UUID.randomUUID() }.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }


    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
    }
}