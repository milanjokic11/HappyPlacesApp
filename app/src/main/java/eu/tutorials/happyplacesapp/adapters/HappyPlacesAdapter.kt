package eu.tutorials.happyplacesapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.happyplacesapp.R
import eu.tutorials.happyplacesapp.activities.AddHappyPlaceActivity
import eu.tutorials.happyplacesapp.activities.MainActivity
import eu.tutorials.happyplacesapp.database.DatabaseHandler
import eu.tutorials.happyplacesapp.models.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var onClickListener: OnClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return MyViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.item_happy_place,
                    parent,
                    false
                )
            )
        }

        fun setOnClickListener(onClickListener: OnClickListener) {
            this.onClickListener = onClickListener
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
                holder.itemView.setOnClickListener{
                    if (onClickListener != null) {
                        onClickListener!!.onClick(position, model)
                    }
                }
            }
        }

        fun removeAt(pos: Int) {
            val dbHandler = DatabaseHandler(context)
            val isDeleted = dbHandler.deleteHappyPlace(list[pos])
            if (isDeleted > 0) {
                list.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }

        fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
            val intent = Intent(context, AddHappyPlaceActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
            activity.startActivityForResult(intent, requestCode)
            notifyItemChanged(position)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        interface OnClickListener {
            fun onClick(pos: Int, model: HappyPlaceModel)
        }
        private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }