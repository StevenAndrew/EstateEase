package com.example.propertymanagementapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.Property

open class PropertyItemsAdapter(private val context: Context,
                                private var list: ArrayList<Property>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_property,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(holder.itemView.findViewById<AppCompatImageView>(R.id.item_property_image))

            holder.itemView.findViewById<TextView>(R.id.tv_property_name).text = model.name
            if (model.status.equals("Rent")){
                holder.itemView.findViewById<TextView>(R.id.tv_property_status).text = "${model.status}: IDR ${model.price}/mo"
            }else{
                holder.itemView.findViewById<TextView>(R.id.tv_property_status).text = "${model.status}: IDR ${model.price}"
            }

            holder.itemView.findViewById<TextView>(R.id.tv_property_created_by).text = "Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Property)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}