package eu.tutorials.happyplacesapp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.Locale

class GetAddressFromLatLng(context: Context, private val lat: Double, private val long: Double): AsyncTask<Void, String, String>() {
    private val geocoder: Geocoder = Geocoder(context, Locale.US)
    private lateinit var mAddressListener: AddressListener


    override fun doInBackground(vararg p0: Void?): String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(lat, long, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                sb.deleteCharAt(sb.length - 1)
                return sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onPostExecute(result: String?) {
        if (result == null)
            mAddressListener.onError()
        else
            mAddressListener.onAddressFound(result)
        super.onPostExecute(result)
    }

    fun setAddressListener(addressListener: AddressListener) {
        mAddressListener = addressListener
    }

    fun getAddress() {
        execute()
    }

    interface AddressListener{
        fun onAddressFound(address: String)
        fun onError()
    }
}