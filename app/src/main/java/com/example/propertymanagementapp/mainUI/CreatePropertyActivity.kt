package com.example.propertymanagementapp.mainUI

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.example.propertymanagementapp.utils.Constants
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreatePropertyActivity : BaseActivity() {

    companion object{
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mPropertyImageURL: String = ""
    private var mAddress: String = ""
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_property)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!Places.isInitialized()){
            Places.initialize(this, resources.getString(R.string.google_maps_api_key))
        }

        setupActionBar()
        if (intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        //open maps when entering address
        findViewById<AppCompatEditText>(R.id.create_property_address).setOnClickListener{
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

        findViewById<AppCompatImageView>(R.id.create_property_place_image).setOnClickListener{

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                //Show Image Chooser
                Constants.showImageChooser(this)
            }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
                //Show Image Chooser
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        findViewById<Button>(R.id.btn_create_property).setOnClickListener{
            if (mSelectedImageFileUri != null &&
                findViewById<AppCompatEditText>(R.id.create_property_name).text.toString().isNotEmpty() &&
                findViewById<AppCompatEditText>(R.id.create_property_description).text.toString().isNotEmpty() &&
                mAddress.isNotEmpty()&&
                findViewById<AppCompatEditText>(R.id.create_property_area).text.toString().isNotEmpty()&&
                findViewById<AppCompatEditText>(R.id.create_property_price).text.toString().isNotEmpty()){
                uploadPropertyImage()
            }else if (mSelectedImageFileUri == null){
                showErrorSnackBar("Please input an image")
            }
            else if (findViewById<AppCompatEditText>(R.id.create_property_name).text.toString().isEmpty()){
                showErrorSnackBar("Please enter a name")
            }
            else if (findViewById<AppCompatEditText>(R.id.create_property_description).text.toString().isEmpty()){
                showErrorSnackBar("Please enter a description")
            }
            else if (mAddress.isEmpty()){
                showErrorSnackBar("Please input an address")
            }
            else if (findViewById<AppCompatEditText>(R.id.create_property_area).text.toString().isEmpty()){
                showErrorSnackBar("Please enter property area")
            }
            else if (findViewById<AppCompatEditText>(R.id.create_property_price).text.toString().isEmpty()){
                showErrorSnackBar("Please enter property price")
            }
        }
    }

    private fun uploadPropertyImage(){
        showProgressDialog()

        if(mSelectedImageFileUri != null){
            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "PROPERTY_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this, mSelectedImageFileUri)
                )     //Firebase Storage Reference, rename to pathString in storage
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i(
                    "Firebase Property Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                    Log.i("Downloadable Image URL", uri.toString())
                    mPropertyImageURL = uri.toString()
                    registerProperty()
                }
            }.addOnFailureListener{
                    exception ->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }else{
            showErrorSnackBar("Please upload an image")
            hideProgressDialog()
        }
    }

    fun propertyCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_create_property_activity))

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_white_arrow_back_24)
            actionBar.title = resources.getString(R.string.add_property_title)
        }
        findViewById<Toolbar>(R.id.toolbar_create_property_activity).setNavigationOnClickListener { onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Show Image Chooser
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(this, "Please grant the permission for storage.", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.add_screen_image_placeholder)
                    .into(findViewById<AppCompatImageView>(R.id.create_property_place_image))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            mAddress = place.address!!
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
            findViewById<AppCompatEditText>(R.id.create_property_address).setText(mAddress)
        }
    }

    //register property on firestore database
    private fun registerProperty(){
        val name: String = findViewById<AppCompatEditText>(R.id.create_property_name).text.toString().trim{it<=' '}
        val description: String = findViewById<AppCompatEditText>(R.id.create_property_description).text.toString().trim{it<=' '}
        val address: String = mAddress
        val area: Long = findViewById<AppCompatEditText>(R.id.create_property_area).text.toString().trim{it<=' '}.toLong()
        val price: Long = findViewById<AppCompatEditText>(R.id.create_property_price).text.toString().trim{it<=' '}.toLong()

        val property = Property(
            userid = getCurrentUserID(),
            name = name,
            createdBy = mUserName,
            image = mPropertyImageURL,
            description = description,
            address = address,
            area = area,
            price = price,
            latitude = mLatitude,
            longitude = mLongitude,
        )
        FirestoreClass().createProperty(this,property)
    }
}
