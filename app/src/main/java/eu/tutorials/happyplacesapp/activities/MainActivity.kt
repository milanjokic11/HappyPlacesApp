package eu.tutorials.happyplacesapp.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.adapters.HappyPlacesAdapter
import eu.tutorials.happyplacesapp.database.DatabaseHandler
import eu.tutorials.happyplacesapp.models.HappyPlaceModel
import eu.tutorials.happyplacesapp.utils.SwipeToDeleteCallback
import eu.tutorials.happyplacesapp.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    private var fabAddHappyPlace: FloatingActionButton? = null
    private var rvHappyPlacesList: RecyclerView? = null
    private var tvNoRecordsAvailable: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddHappyPlace = findViewById(R.id.fab_add_happy_place)
        fabAddHappyPlace!!.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_REQUEST_CODE)
        }
        rvHappyPlacesList = findViewById(R.id.rv_happy_places_list)
        tvNoRecordsAvailable = findViewById(R.id.tv_no_records_available)
        getHappyPlacesListFromDB()
    }

    private fun setUpHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        rvHappyPlacesList?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        rvHappyPlacesList?.adapter = placesAdapter
        placesAdapter.setOnClickListener(object: HappyPlacesAdapter.OnClickListener{
            override fun onClick(pos: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        // set up swipe right to edit happy place
        val editSwipeHandler = object: SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvHappyPlacesList)

        // set up swipe left to delete happy place
        val deleteSwipeHandler = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getHappyPlacesListFromDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvHappyPlacesList)
    }

    private fun getHappyPlacesListFromDB() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
        if (getHappyPlaceList.size > 0) {
            rvHappyPlacesList?.visibility = View.VISIBLE
            tvNoRecordsAvailable?.visibility = View.GONE
            setUpHappyPlacesRecyclerView(getHappyPlaceList)
        } else {
            rvHappyPlacesList?.visibility = View.GONE
            tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    @Deprecated("Deprecated onActivityResult(requestCode, resultCode, data) in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromDB()
            } else {
                Log.e("Activity", "Cancelled or back was pressed")
            }
        }
    }

    companion object {
        var ADD_PLACE_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}