package com.example.propertymanagementapp.mainUI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.propertymanagementapp.data.Meeting
import com.example.propertymanagementapp.firebase.FirestoreClass

class MeetingViewActivity : BaseActivity() {

    lateinit var mMeetingDetails: Meeting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meeting_view)
        setupActionBar()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var meetingId = ""
        if (intent.hasExtra("id")){
            meetingId = intent.getStringExtra("id")!!
        }
        showProgressDialog()
        FirestoreClass().getMeetingDetails(this, meetingId)
        findViewById<Button>(R.id.btn_open_in_maps_meeting).setOnClickListener{
            val geoUri: String = "http://maps.google.com/maps?q=loc:"+mMeetingDetails.latitude+","+mMeetingDetails.longitude+"("+mMeetingDetails.location+")"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            startActivity(intent)
        }

//        findViewById<Button>(R.id.btn_edit_property).setOnClickListener{
//            val intent = Intent(this, PropertyEditActivity::class.java)
//            intent.putExtra("id", propertyId)
//            startActivity(intent)
//        }
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_meeting_view_activity))
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_white_arrow_back_24)
            actionBar.title = "Meeting Info"
        }
        findViewById<Toolbar>(R.id.toolbar_meeting_view_activity).setNavigationOnClickListener { onBackPressed() }
    }

    //populate UI with meeting details
    fun meetingDetails(meeting: Meeting){
        hideProgressDialog()
        mMeetingDetails = meeting
        var hour : String = "00"
        var minute : String = "00"
        if (meeting.hour<10){
            hour = "0${meeting.hour}"
        }else{
            hour = meeting.hour.toString()
        }

        if (meeting.minute<10){
            minute = "0${meeting.minute}"
        }else{
            minute = meeting.minute.toString()
        }
        Glide
            .with(this)
            .load(meeting.propertyImage)
            .centerCrop()
            .placeholder(R.drawable.add_screen_image_placeholder)
            .into(findViewById<ImageView>(R.id.iv_meeting_details_image))

        findViewById<TextView>(R.id.property_meet_name).setText("Property: " + meeting.propertyName)
        findViewById<TextView>(R.id.meeting_view_address).setText("Meeting Location: " +meeting.location)
        findViewById<TextView>(R.id.meeting_view_date).setText("Date: ${meeting.day}-${meeting.month}")
        findViewById<TextView>(R.id.meeting_view_time).setText("Time: $hour:$minute")
    }
}