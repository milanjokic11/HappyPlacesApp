package eu.tutorials.happyplacesapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.models.HappyPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mHappyPlaceDetails: HappyPlaceModel? = null
    private var mapToolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }
        if (mHappyPlaceDetails != null) {
            mapToolbar = findViewById(R.id.happy_place_map_toolbar)
            setSupportActionBar(mapToolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetails!!.title
            mapToolbar!!.setNavigationOnClickListener {
                onBackPressed()
            }
            val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        val pos = LatLng(mHappyPlaceDetails!!.latitude, mHappyPlaceDetails!!.longitude)
        map!!.addMarker(MarkerOptions().position(pos).title(mHappyPlaceDetails!!.location))
        // code to zoom into location on google map
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(pos, 11f)
        map.animateCamera(newLatLngZoom)
    }
}