package com.example.propertymanagementapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.Meeting
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.firebase.FirestoreClass

open class MeetingItemsAdapter(private val context: Context,
                                private var list: ArrayList<Meeting>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_meeting,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.propertyImage)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(holder.itemView.findViewById<AppCompatImageView>(R.id.item_meeting_image))

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_name).text = "Property: ${model.propertyName}"

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_created_by).text = "Meeting Recipient: ${model.meetingCreatorName}"

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_location).text = "Meeting Location: ${model.location}"

            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Meeting)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}