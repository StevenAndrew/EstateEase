package com.example.propertymanagementapp.mainUI

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.Meeting
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.example.propertymanagementapp.utils.Constants
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RescheduleMeetingActivity : BaseActivity() {

    companion object{
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 66
    }

    private lateinit var mPropertyDetails: Property
    private lateinit var mMeetingDetails: Meeting
    var meetingIdEdit = ""
    private var mAddress: String = ""
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private val calendar = Calendar.getInstance()

    private var mDate: Long = 0;
    private var mMonth: Long = 0;
    private var mYear: Long = 0;
    private var mHour: Long = 0;
    private var mMinute: Long = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reschedule_meeting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActionBar()
        if (intent.hasExtra("id")){
            meetingIdEdit = intent.getStringExtra("id")!!
        }
        showProgressDialog()
        FirestoreClass().getMeetingDetails(this, meetingIdEdit)

        if (!Places.isInitialized()){
            Places.initialize(this, resources.getString(R.string.google_maps_api_key))
        }

        findViewById<AppCompatEditText>(R.id.reschedule_meeting_location).setOnClickListener{
            try {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                startActivityForResult(intent,
                    RescheduleMeetingActivity.PLACE_AUTOCOMPLETE_REQUEST_CODE
                )
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        findViewById<Button>(R.id.reschedule_btn_date).setOnClickListener{
            showDatePicker()
        }

        findViewById<Button>(R.id.reschedule_btn_time).setOnClickListener{
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                mHour = calendar.get(Calendar.HOUR_OF_DAY).toLong()
                mMinute = calendar.get(Calendar.MINUTE).toLong()
                val time = SimpleDateFormat("HH:mm").format(calendar.time)
                findViewById<TextView>(R.id.reschedule_meeting_time).text = "Time: $time"
            }

            TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        findViewById<Button>(R.id.btn_set_same_location_reschedule).setOnClickListener{
            FirestoreClass().getPropertyDetails(this,mMeetingDetails.propertyid)
        }


        findViewById<Button>(R.id.btn_reschedule_meeting).setOnClickListener{
            showProgressDialog()
            updateMeeting()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_reschedule_meeting_activity))
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_white_arrow_back_24)
            actionBar.title = "Reschedule Meeting"
        }
        findViewById<Toolbar>(R.id.toolbar_reschedule_meeting_activity).setNavigationOnClickListener { onBackPressed() }
    }

    fun setLocationAsProperty(property: Property){
        mLongitude = property.longitude
        mLatitude = property.latitude
        mAddress = property.address
        findViewById<AppCompatEditText>(R.id.reschedule_meeting_location).setText("${property.address}")
    }

    //populate UI with property details
    fun updateMeetingDetails(meeting: Meeting){
        mMeetingDetails = meeting
        mLatitude = meeting.latitude
        mLongitude = meeting.longitude

        mDate = meeting.day
        mMonth = meeting.month
        mYear = meeting.year
        mHour = meeting.hour
        mMinute = meeting.minute
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
        hideProgressDialog()
        Glide
            .with(this)
            .load(meeting.propertyImage)
            .centerCrop()
            .placeholder(R.drawable.add_screen_image_placeholder)
            .into(findViewById<ImageView>(R.id.reschedule_meeting_property_image))
        findViewById<AppCompatEditText>(R.id.reschedule_meeting_location).setText("${meeting.location}")
        findViewById<TextView>(R.id.reschedule_meeting_date).setText("Date: ${meeting.day}-${meeting.month}")
        findViewById<TextView>(R.id.reschedule_meeting_time).setText("Time: $hour:$minute")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            mAddress = place.address!!
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
            findViewById<AppCompatEditText>(R.id.reschedule_meeting_location).setText(mAddress)
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                mDate = dayOfMonth.toLong()
                mMonth = monthOfYear.toLong() + 1
                mYear = year.toLong()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                findViewById<TextView>(R.id.reschedule_meeting_date).text = "Date: $formattedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    //create property hashmap
    private fun updateMeeting(){
        val meetingHashMap = HashMap<String, Any>()

        if (findViewById<AppCompatEditText>(R.id.reschedule_meeting_location).text.toString() != mMeetingDetails.location){
            meetingHashMap["location"] = findViewById<AppCompatEditText>(R.id.reschedule_meeting_location).text.toString()
        }

        if (mLatitude != mMeetingDetails.latitude){
            meetingHashMap["latitude"] = mLatitude
        }

        if (mLongitude != mMeetingDetails.longitude){
            meetingHashMap["longitude"] = mLongitude
        }

        if (getCurrentUserID()==mMeetingDetails.userid){
            meetingHashMap["statusCreator"] = "Pending"
            meetingHashMap["statusOwner"] = "Awaiting Confirmation"
        }else if(getCurrentUserID()==mMeetingDetails.ownerid){
            meetingHashMap["statusCreator"] = "Awaiting Confirmation"
            meetingHashMap["statusOwner"] = "Pending"
        }

        meetingHashMap["year"] = mYear
        meetingHashMap["month"] = mMonth
        meetingHashMap["day"] = mDate
        meetingHashMap["hour"] = mHour
        meetingHashMap["minute"] = mMinute


        FirestoreClass().rescheduleMeetingData(this, meetingHashMap, meetingIdEdit)
    }

    //move to MainActivity when done
    fun meetingRescheduleSuccess(){
        hideProgressDialog()
        startActivity(Intent(this, MyMeetingActivity::class.java))
        finish()
    }
}