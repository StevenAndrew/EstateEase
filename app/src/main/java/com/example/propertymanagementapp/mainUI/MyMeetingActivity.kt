package com.example.propertymanagementapp.mainUI

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.adapters.MeetingItemsAdapter
import com.example.propertymanagementapp.adapters.PropertyItemsAdapter
import com.example.propertymanagementapp.data.Meeting
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.example.propertymanagementapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyMeetingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_meetings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActionBar()
        showProgressDialog()
        FirestoreClass().getUserMeetingList(this)
    }


    private fun setupActionBar(){
        setSupportActionBar(findViewById(R.id.toolbar_my_meeting_activity))

        val actionBar = supportActionBar

        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_white_arrow_back_24)
            actionBar.title = "My Meetings"
        }
        findViewById<Toolbar>(R.id.toolbar_my_meeting_activity).setNavigationOnClickListener{
            val intent = Intent(this@MyMeetingActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //edit for meeting
    fun populateBoardsListToUI(meetingList: ArrayList<Meeting>){
        hideProgressDialog()

        if (meetingList.size>0){
            //if there are properties in the database, set list to visible and remove the no properties available text
            findViewById<RecyclerView>(R.id.rv_meeting_list).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_no_meetings_available).visibility = View.GONE

            findViewById<RecyclerView>(R.id.rv_meeting_list).layoutManager = LinearLayoutManager(this)
            findViewById<RecyclerView>(R.id.rv_meeting_list).setHasFixedSize(true)

            val adapter = MeetingItemsAdapter(this, meetingList)
            findViewById<RecyclerView>(R.id.rv_meeting_list).adapter = adapter

            adapter.setOnClickListener(object: MeetingItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Meeting) {
                    val intent = Intent(this@MyMeetingActivity, MeetingViewActivity::class.java)   // change after creating meetingview
                    intent.putExtra("id", model.id)
                    startActivity(intent)
                }
            })

        }else{
            findViewById<RecyclerView>(R.id.rv_meeting_list).visibility = View.GONE
            findViewById<TextView>(R.id.tv_no_meetings_available).visibility = View.VISIBLE
        }
    }
}