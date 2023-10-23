package eu.tutorials.happyplacesapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.models.HappyPlaceModel

class HappyPlaceDetailActivity: AppCompatActivity() {
    private var happyPlaceDetailToolbar: Toolbar? = null
    private var ivPlaceImage: ImageView? = null
    private var tvDescription: TextView? = null
    private var tvLocation: TextView? = null
    private var btnShowMap: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)

        var happyPlaceDetailModel: HappyPlaceModel? = null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)!! as HappyPlaceModel
        }
        if (happyPlaceDetailModel != null) {
            happyPlaceDetailToolbar = findViewById<Toolbar>(R.id.happy_place_detail_toolbar)
            ivPlaceImage = findViewById(R.id.iv_place_image)
            tvDescription = findViewById(R.id.tv_description)
            tvLocation = findViewById(R.id.tv_location)
            setSupportActionBar(happyPlaceDetailToolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title
            happyPlaceDetailToolbar?.setNavigationOnClickListener {
                onBackPressed()
            }
            ivPlaceImage?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            tvDescription?.text = happyPlaceDetailModel.description
            tvLocation?.text = happyPlaceDetailModel.location
            // button to show location via map
            btnShowMap = findViewById(R.id.btn_map_view)
            btnShowMap?.setOnClickListener {
                val intent = Intent(this@HappyPlaceDetailActivity, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
                startActivity(intent)
            }
        }
    }
}