package com.ajith.smilestatusaver

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StatusAdapter(
    private val context: Context,
    private var modelClass: ArrayList<ModelClass>, private val clickListener: (ModelClass) -> Unit
) :

    RecyclerView.Adapter<StatusAdapter.StatusViewholder>() {

    class StatusViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivStatus: ImageView = itemView.findViewById(R.id.image_iv_status)
        val ivVideoIcon: ImageView = itemView.findViewById(R.id.video_icon_img)
        val cvVideoCard: CardView = itemView.findViewById(R.id.video_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewholder {
        return StatusViewholder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_for_status, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return modelClass.size
    }

    override fun onBindViewHolder(holder: StatusViewholder, position: Int) {

        if (modelClass[position].fileUri.endsWith(".mp4")) {
            holder.cvVideoCard.visibility = View.VISIBLE
            holder.ivVideoIcon.visibility = View.VISIBLE
        } else {
            holder.cvVideoCard.visibility = View.GONE
            holder.ivVideoIcon.visibility = View.GONE
        }

        Glide.with(context).load(Uri.parse(modelClass[position].fileUri)).into(holder.ivStatus)

        holder.ivStatus.setOnClickListener{
            clickListener(modelClass[position])
        }

    }


}