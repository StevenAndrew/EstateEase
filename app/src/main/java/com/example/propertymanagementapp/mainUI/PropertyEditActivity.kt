package com.example.propertymanagementapp.mainUI

import android.Manifest
import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
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

class PropertyEditActivity : BaseActivity() {

    companion object{
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 66
    }

    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mPropertyDetails: Property
    private var mPropertyImageURL: String = ""
    var propertyIdEdit = ""
    private var mAddress: String = ""
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_property_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActionBar()
        if (intent.hasExtra("id")){
            propertyIdEdit = intent.getStringExtra("id")!!
        }
        showProgressDialog()
        FirestoreClass().getPropertyDetails(this, propertyIdEdit)

        if (!Places.isInitialized()){
            Places.initialize(this, resources.getString(R.string.google_maps_api_key))
        }

        findViewById<AppCompatEditText>(R.id.et_address_edit_property).setOnClickListener{
            try {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                startActivityForResult(intent,
                    PropertyEditActivity.PLACE_AUTOCOMPLETE_REQUEST_CODE
                )
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        findViewById<ImageView>(R.id.iv_property_details_image).setOnClickListener{
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

        findViewById<Button>(R.id.btn_update_property).setOnClickListener{
            if (mSelectedImageFileUri != null){
                showProgressDialog()
                uploadPropertyImage()
            }else{
                showProgressDialog()
                updateProperty()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_property_edit_activity))
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_white_arrow_back_24)
            actionBar.title = resources.getString(R.string.edit_property)
        }
        findViewById<Toolbar>(R.id.toolbar_property_edit_activity).setNavigationOnClickListener { onBackPressed() }
    }

    //populate UI with property details
    fun updatePropertyDetails(property: Property){
        mPropertyDetails = property
        mLatitude = property.latitude
        mLongitude = property.longitude
        hideProgressDialog()
        Glide
            .with(this)
            .load(property.image)
            .centerCrop()
            .placeholder(R.drawable.add_screen_image_placeholder)
            .into(findViewById<ImageView>(R.id.iv_property_details_image))
        findViewById<AppCompatEditText>(R.id.et_name_edit_property).setText(property.name)
        findViewById<AppCompatEditText>(R.id.et_description_edit_property).setText(property.description)
        findViewById<AppCompatEditText>(R.id.et_address_edit_property).setText(property.address)
        findViewById<AppCompatEditText>(R.id.et_area_edit_property).setText(property.area.toString())
        findViewById<AppCompatEditText>(R.id.et_price_edit_property).setText(property.price.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Show Image Chooser
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(this,"Please grant the permission for storage.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@PropertyEditActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.add_screen_image_placeholder)
                    .into(findViewById<CircleImageView>(R.id.iv_property_details_image))
            }catch (e: IOException){
                e.printStackTrace()
            }
        }else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            mAddress = place.address!!
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
            findViewById<AppCompatEditText>(R.id.et_address_edit_property).setText(mAddress)
        }
    }

    //create property hashmap
    private fun updateProperty(){
        val propertyHashMap = HashMap<String, Any>()

        if (mPropertyImageURL.isNotEmpty() && mPropertyImageURL != mPropertyDetails.image){
            propertyHashMap["image"] = mPropertyImageURL
        }

        if (findViewById<AppCompatEditText>(R.id.et_name_edit_property).text.toString() != mPropertyDetails.name){
            propertyHashMap["name"] = findViewById<AppCompatEditText>(R.id.et_name_edit_property).text.toString()
        }

        if (findViewById<AppCompatEditText>(R.id.et_description_edit_property).text.toString() != mPropertyDetails.description){
            propertyHashMap["description"] = findViewById<AppCompatEditText>(R.id.et_description_edit_property).text.toString()
        }

        if (findViewById<AppCompatEditText>(R.id.et_address_edit_property).text.toString() != mPropertyDetails.address){
            propertyHashMap["address"] = findViewById<AppCompatEditText>(R.id.et_address_edit_property).text.toString()
        }

        if (findViewById<AppCompatEditText>(R.id.et_area_edit_property).toString() != mPropertyDetails.area.toString()){
            propertyHashMap["area"] = findViewById<AppCompatEditText>(R.id.et_area_edit_property).text.toString().toLong()
        }

        if (findViewById<AppCompatEditText>(R.id.et_price_edit_property).toString() != mPropertyDetails.price.toString()){
            propertyHashMap["price"] = findViewById<AppCompatEditText>(R.id.et_price_edit_property).text.toString().toLong()
        }

        if (mLatitude != mPropertyDetails.latitude){
            propertyHashMap["latitude"] = mLatitude
        }

        if (mLongitude != mPropertyDetails.longitude){
            propertyHashMap["longitude"] = mLongitude
        }

        FirestoreClass().updatePropertyData(this, propertyHashMap, propertyIdEdit)
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
                    updateProperty()
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

    //move to MainActivity when done
    fun propertyUpdateSuccess(){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}