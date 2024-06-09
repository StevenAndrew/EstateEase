package com.example.propertymanagementapp.mainUI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.firebase.FirestoreClass

class PropertyViewActivity : BaseActivity() {

    lateinit var mPropertyDetails: Property
    lateinit var mPropertyOwnerDetails: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_property_view)
        setupActionBar()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var propertyId = ""
        if (intent.hasExtra("id")){
            propertyId = intent.getStringExtra("id")!!
        }
        showProgressDialog()
        FirestoreClass().getPropertyDetails(this, propertyId)
        findViewById<Button>(R.id.btn_open_in_maps).setOnClickListener{
            val geoUri: String = "http://maps.google.com/maps?q=loc:"+mPropertyDetails.latitude+","+mPropertyDetails.longitude+"("+mPropertyDetails.address+")"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_edit_property).setOnClickListener{
            val intent = Intent(this, PropertyEditActivity::class.java)
            intent.putExtra("id", propertyId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_delete_property).setOnClickListener{
            showProgressDialog()
            FirestoreClass().deletePropertyData(this, propertyId)
        }

        findViewById<ImageView>(R.id.iv_property_details_image).setOnClickListener{
            findViewById<ImageView>(R.id.iv_property_details_zoom_image).visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_zoom_out_property).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.iv_property_details_image).visibility = View.GONE

            Glide
                .with(this)
                .load(mPropertyDetails.image)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(findViewById<ImageView>(R.id.iv_property_details_zoom_image))

            findViewById<Button>(R.id.btn_zoom_out_property).setOnClickListener{
                findViewById<ImageView>(R.id.iv_property_details_zoom_image).visibility = View.GONE
                findViewById<Button>(R.id.btn_zoom_out_property).visibility = View.GONE
                findViewById<ImageView>(R.id.iv_property_details_image).visibility = View.VISIBLE
            }
        }




    }

    fun onDeletePropertySuccess(){
        hideProgressDialog()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_property_view_activity))
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_white_arrow_back_24)
            actionBar.title = resources.getString(R.string.property_info)
        }
        findViewById<Toolbar>(R.id.toolbar_property_view_activity).setNavigationOnClickListener { onBackPressed() }
    }

    fun propertyDetails(property: Property){
        hideProgressDialog()
        mPropertyDetails = property
        Glide
            .with(this)
            .load(property.image)
            .centerCrop()
            .placeholder(R.drawable.add_screen_image_placeholder)
            .into(findViewById<ImageView>(R.id.iv_property_details_image))

        findViewById<TextView>(R.id.property_view_name).setText("Name: " + property.name)
        findViewById<TextView>(R.id.property_view_description).setText("Property Description: " +property.description)
        findViewById<TextView>(R.id.property_view_address).setText("Property Address: " +property.address)
        findViewById<TextView>(R.id.property_view_area).setText("Property Area in Meter2: " + property.area.toString())
        findViewById<TextView>(R.id.property_view_price).setText("Property Price in Rupiah: " + property.price.toString())
    }

    fun userDetailsFromProperty(user: User){
        hideProgressDialog()
        mPropertyOwnerDetails = user

        findViewById<TextView>(R.id.property_view_owner_mobile).setText("Owner Mobile Phone Number: " + user.mobileNumber.toString())
        findViewById<TextView>(R.id.property_view_owner_email).setText("Owner Email: " +user.email)
    }
}