package com.example.propertymanagementapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        var hour : String = "00"
        var minute : String = "00"
        if (model.hour<10){
            hour = "0${model.hour}"
        }else{
            hour = model.hour.toString()
        }

        if (model.minute<10){
            minute = "0${model.minute}"
        }else{
            minute = model.minute.toString()
        }
        if (holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.propertyImage)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(holder.itemView.findViewById<AppCompatImageView>(R.id.item_meeting_image))

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_name).text = "${model.propertyName}"

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_created_by).text = "Meeting Recipient: ${model.meetingCreatorName}"

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_location).text = "Meeting Location: ${model.location}"

            if (model.ownerid == FirestoreClass().getCurrentUserID()){
                holder.itemView.findViewById<TextView>(R.id.tv_meeting_status).text = "Status: ${model.statusOwner}"
            }else{
                holder.itemView.findViewById<TextView>(R.id.tv_meeting_status).text = "Status: ${model.statusCreator}"
            }

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_date).text = "Date: ${model.day}-${model.month}"

            holder.itemView.findViewById<TextView>(R.id.tv_meeting_time).text = "Time: $hour:$minute"

            if ((model.ownerid == FirestoreClass().getCurrentUserID() && model.statusOwner == "Awaiting Confirmation") || (model.userid == FirestoreClass().getCurrentUserID() && model.statusCreator == "Awaiting Confirmation")){
                holder.itemView.findViewById<Button>(R.id.btn_accept_meeting).visibility = View.VISIBLE
                holder.itemView.findViewById<Button>(R.id.btn_decline_meeting).visibility = View.VISIBLE
            }

            if (model.statusOwner != "Declined" && model.statusCreator != "Declined"){
                holder.itemView.setOnClickListener {
                    if (onClickListener!=null){
                        onClickListener!!.onClick(position, model)
                    }
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