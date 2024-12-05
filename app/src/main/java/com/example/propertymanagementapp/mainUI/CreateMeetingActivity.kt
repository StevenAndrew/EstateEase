package com.example.propertymanagementapp.mainUI

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.R.*
import com.example.propertymanagementapp.data.Meeting
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.example.propertymanagementapp.utils.Constants
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateMeetingActivity : BaseActivity() {

    companion object{
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }

    private lateinit var mPropertyDetails: Property
    lateinit var mPropertyOwnerDetails: User
    private var propertyIdMeet = ""
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
        setContentView(layout.activity_create_meeting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!Places.isInitialized()){
            Places.initialize(this, resources.getString(string.google_maps_api_key))
        }

        setupActionBar()
        if (intent.hasExtra("id")){
            propertyIdMeet = intent.getStringExtra("id")!!
        }
        showProgressDialog()
        FirestoreClass().getPropertyDetails(this, propertyIdMeet)

        //open maps when entering location
        findViewById<AppCompatEditText>(id.create_meeting_location).setOnClickListener{
            try {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        findViewById<Button>(id.btn_create_meeting).setOnClickListener{
            if (mAddress.isNotEmpty()&&
                findViewById<TextView>(id.create_meeting_date).text.toString().isNotEmpty()&&
                findViewById<TextView>(id.create_meeting_time).text.toString().isNotEmpty()){
                registerMeeting()
            }
            else if (mAddress.isEmpty()){
                showErrorSnackBar("Please enter meeting location")
            }else if (findViewById<AppCompatEditText>(id.create_meeting_date).text.toString().isEmpty()){
                showErrorSnackBar("Please enter meeting date")
            }else if (findViewById<AppCompatEditText>(id.create_meeting_time).text.toString().isEmpty()){
                showErrorSnackBar("Please enter meeting time")
            }
        }

        findViewById<Button>(id.btn_date).setOnClickListener{
            showDatePicker()
        }

        findViewById<Button>(id.btn_time).setOnClickListener{
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                mHour = calendar.get(Calendar.HOUR_OF_DAY).toLong()
                mMinute = calendar.get(Calendar.MINUTE).toLong()
                val time = SimpleDateFormat("HH:mm").format(calendar.time)
                findViewById<TextView>(id.create_meeting_time).text = "Selected Time: $time"
            }

            TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        findViewById<Button>(id.btn_set_same_location).setOnClickListener{
            mAddress = mPropertyDetails.address
            mLatitude = mPropertyDetails.latitude
            mLongitude = mPropertyDetails.longitude
            findViewById<AppCompatEditText>(id.create_meeting_location).setText(mAddress)
        }
    }

    private fun showDatePicker() {
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                mDate = dayOfMonth.toLong()
                mMonth = monthOfYear.toLong() + 1
                mYear = year.toLong()
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                findViewById<TextView>(id.create_meeting_date).text = "Selected Date: $formattedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(id.toolbar_create_meeting_activity))

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(drawable.baseline_white_arrow_back_24)
            actionBar.title = resources.getString(string.create_meeting_title)
        }
        findViewById<Toolbar>(id.toolbar_create_meeting_activity).setNavigationOnClickListener { onBackPressed() }
    }

    fun propertyImage(property: Property){
        hideProgressDialog()
        mPropertyDetails = property
        Glide
            .with(this)
            .load(property.image)
            .centerCrop()
            .placeholder(R.drawable.add_screen_image_placeholder)
            .into(findViewById<ImageView>(R.id.create_meeting_property_image))
    }

    fun userDetailsFromProperty(user: User){
        hideProgressDialog()
        mPropertyOwnerDetails = user

        findViewById<TextView>(R.id.meeting_owner_name).setText("Owner: " + user.name.toString())
        findViewById<TextView>(R.id.meeting_owner_phone).setText("Phone No: " +user.mobileNumber)
    }

    fun meetingCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            mAddress = place.address!!
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
            findViewById<AppCompatEditText>(id.create_meeting_location).setText(mAddress)
        }
    }

    //register meeting on firestore database
    private fun registerMeeting(){
        val address: String = mAddress

        val meeting = Meeting(
            userid = getCurrentUserID(),
            ownerid = mPropertyOwnerDetails.id,
            propertyid = propertyIdMeet,
            location = address,
            latitude = mLatitude,
            longitude = mLongitude,
            year = mYear,
            month = mMonth,
            day = mDate,
            hour = mHour,
            minute = mMinute,
            status = "Pending"
        )

            FirestoreClass().createMeeting(this,meeting)
    }
}
