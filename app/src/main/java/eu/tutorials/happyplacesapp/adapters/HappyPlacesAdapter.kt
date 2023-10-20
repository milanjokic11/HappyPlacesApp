package eu.tutorials.happyplacesapp.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.models.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return MyViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.item_happy_place,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val model = list[position]
            val ivPlaceImage: ImageView
            val tvTitle: TextView
            val tvDescription: TextView
            if (holder is MyViewHolder) {
                ivPlaceImage = holder.itemView.findViewById(R.id.iv_place_image)
                tvTitle = holder.itemView.findViewById(R.id.tv_title)
                tvDescription = holder.itemView.findViewById(R.id.tv_description)
                // set values
                ivPlaceImage.setImageURI(Uri.parse(model.image))
                tvTitle.text = model.title
                tvDescription.text = model.description
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }