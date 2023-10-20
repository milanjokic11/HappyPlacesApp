package eu.tutorials.happyplacesapp.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.database.DatabaseHandler
import eu.tutorials.happyplacesapp.models.HappyPlaceModel
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
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        addPlaceToolbar = findViewById(R.id.add_place_toolbar)

        setSupportActionBar(addPlaceToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addPlaceToolbar!!.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }
        updateDateView()
        etDate = findViewById<EditText>(R.id.et_date)
        etDate?.setOnClickListener(this)
        tvAddImage = findViewById<TextView>(R.id.tv_add_image)
        tvAddImage?.setOnClickListener(this)
        ivPlaceImage = findViewById<ImageView>(R.id.iv_place_image)
        etTitle = findViewById<EditText>(R.id.et_title)
        etDescription = findViewById<EditText>(R.id.et_description)
        etLocation = findViewById<EditText>(R.id.et_location)
        btnSave = findViewById<Button>(R.id.btn_save)
        btnSave?.setOnClickListener(this)
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
                            0,
                            etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription?.text.toString(),
                            etDate?.text.toString(),
                            etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                        if (addHappyPlace > 0) {
                            Toast.makeText(this@AddHappyPlaceActivity, "The happy place has been successfully inserted...", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }

            }
        }
    }

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
    }
}