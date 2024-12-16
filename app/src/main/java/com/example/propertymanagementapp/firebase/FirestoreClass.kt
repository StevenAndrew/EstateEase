package com.example.propertymanagementapp.firebase

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.propertymanagementapp.LoginActivities.SignInActivity
import com.example.propertymanagementapp.LoginActivities.SignUpActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.adapters.MeetingItemsAdapter
import com.example.propertymanagementapp.data.Meeting
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.mainUI.CreateMeetingActivity
import com.example.propertymanagementapp.mainUI.CreatePropertyActivity
import com.example.propertymanagementapp.mainUI.MainActivity
import com.example.propertymanagementapp.mainUI.MyMeetingActivity
import com.example.propertymanagementapp.mainUI.MyProfileActivity
import com.example.propertymanagementapp.mainUI.PropertyEditActivity
import com.example.propertymanagementapp.mainUI.PropertyViewActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions


class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    //user database
    fun registerUser(activity: SignUpActivity, userInfo : User){    //register user into the Firestore database
        mFireStore.collection("Users")  //Users is the name of database
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun loadUserData(activity: Activity, readPropertyList: Boolean = false){
        mFireStore.collection("Users")
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {document ->
                val loggedInUser = document.toObject(User::class.java)

                when (activity){
                    is SignInActivity -> {
                    if (loggedInUser != null) {
                        activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity ->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readPropertyList)
                        }
                    }
                    is MyProfileActivity ->{
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }.addOnFailureListener{
                e->
                when (activity){
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e("SignInUser","Error writing document",e)
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        mFireStore.collection("Users")  //Users is the name of database
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")
                Toast.makeText(activity, "Profile Data updated successfully!", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error when updating the profile",e)
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_LONG).show()
            }
    }

    fun getCurrentUserID(): String{     //get current users ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser!=null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    //property database
    fun createProperty(activity: CreatePropertyActivity, propertyInfo : Property){    //register user into the Firestore database
        mFireStore.collection("Property")  //Users is the name of database
            .document()
            .set(propertyInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Property added successfully")
                Toast.makeText(activity,"Property added successfully", Toast.LENGTH_SHORT).show()
                activity.propertyCreatedSuccessfully()
            }.addOnFailureListener{
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error adding property", it)
                Toast.makeText(activity,"Property added successfully", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAllPropertyList(activity: MainActivity){
        mFireStore.collection("Property")
            .get()
            .addOnSuccessListener {
                document->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val propertyList: ArrayList<Property> = ArrayList()
                for (i in document.documents){
                    val property = i.toObject(Property::class.java)!!
                    property.id = i.id
                    propertyList.add(property)
                }

                activity.populateBoardsListToUI(propertyList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error getting the property list", it)
            }
    }

    fun getPropertyDetails(activity: Activity, propertyId: String){
        mFireStore.collection("Property")
            .document(propertyId)
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                when (activity){
                    is PropertyViewActivity -> {
                        activity.propertyDetails(document.toObject(Property::class.java)!!)
                        loadUserDataInProperty(activity, document.toObject(Property::class.java)!!)
                    }
                    is PropertyEditActivity ->{
                        activity.updatePropertyDetails(document.toObject(Property::class.java)!!)
                    }
                    is CreateMeetingActivity -> {
                        activity.propertyImage(document.toObject(Property::class.java)!!)
                        loadUserDataInProperty(activity, document.toObject(Property::class.java)!!)
                        loadCurrentUserData(activity)
                    }
                }
            }.addOnFailureListener{
                when (activity){
                    is PropertyViewActivity -> {
                        activity.hideProgressDialog()
                    }
                    is PropertyEditActivity ->{
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun loadUserDataInProperty(activity: Activity, propertyInfo: Property){
        mFireStore.collection("Users")
            .document(propertyInfo.userid)
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                when (activity){
                    is PropertyViewActivity -> {
                        activity.userDetailsFromProperty(document.toObject(User::class.java)!!)
                        if(propertyInfo.userid == getCurrentUserID()){
                            activity.findViewById<Button>(R.id.btn_edit_property).visibility = View.VISIBLE
                            activity.findViewById<Button>(R.id.btn_delete_property).visibility = View.VISIBLE
                            activity.findViewById<Button>(R.id.btn_meeting).visibility = View.GONE
                        }else{
                            activity.findViewById<Button>(R.id.btn_edit_property).visibility = View.GONE
                            activity.findViewById<Button>(R.id.btn_delete_property).visibility = View.GONE
                            activity.findViewById<Button>(R.id.btn_meeting).visibility = View.VISIBLE
                        }
                    }
                    is CreateMeetingActivity -> {
                        activity.userDetailsFromProperty(document.toObject(User::class.java)!!)
                    }
                }

            }.addOnFailureListener{
                when (activity){
                    is PropertyViewActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun updatePropertyData(activity: PropertyEditActivity, propertyHashMap: HashMap<String, Any>, propertyId: String){
        mFireStore.collection("Property")
            .document(propertyId)
            .update(propertyHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Property updated successfully!")
                Toast.makeText(activity, "Property updated successfully!", Toast.LENGTH_LONG).show()
                activity.propertyUpdateSuccess()
            }.addOnFailureListener {
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while updating the Property",e)
                Toast.makeText(activity, "Error when updating the Property!", Toast.LENGTH_LONG).show()
            }
    }

    fun deletePropertyData(activity: PropertyViewActivity, propertyId: String){
        mFireStore.collection("Property")
            .document(propertyId)
            .delete()
            .addOnSuccessListener {
                activity.onDeletePropertySuccess()
                Toast.makeText(activity, "Property deleted successfully!", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error deleting the Property",e)
                Toast.makeText(activity, "Error deleting Property!", Toast.LENGTH_SHORT).show()
            }
    }

    fun filterPropertyList(activity: MainActivity, status: String, minRooms: Long, maxRooms: Long, minArea: Long, maxArea: Long, minPrice: Long, maxPrice: Long){
        val firestore = mFireStore.collection("Property")
        var query : Query = firestore
        if (status.equals("Sell") || status.equals("Rent")){
            query = query.whereEqualTo("status",status)
        }
        if (minRooms.toInt() != -1){
            query = query.whereGreaterThanOrEqualTo("rooms",minRooms)
        }
        if (maxRooms.toInt() != -1){
            query = query.whereLessThanOrEqualTo("rooms",maxRooms)
        }
        if (minArea.toInt() != -1){
            query = query.whereGreaterThanOrEqualTo("area",minArea)
        }
        if (maxArea.toInt() != -1){
            query = query.whereLessThanOrEqualTo("area",maxArea)
        }
        if (minPrice.toInt() != -1){
            query = query.whereGreaterThanOrEqualTo("price",minPrice)
        }
        if (maxPrice.toInt() != -1){
            query = query.whereLessThanOrEqualTo("price",maxPrice)
        }
            query.get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val propertyList: ArrayList<Property> = ArrayList()
                for (i in document.documents){
                    val property = i.toObject(Property::class.java)!!
                    property.id = i.id
                    propertyList.add(property)
                }
                activity.populateBoardsListToUI(propertyList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error getting the property list", it)
            }
    }

    fun searchPropertyList(activity: MainActivity, name: String){
        var query : Query = mFireStore.collection("Property")
        query = query.whereGreaterThanOrEqualTo("name", name.capitalize())
        query = query.whereLessThan("name", name.capitalize() + "z")

        query.get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val propertyList: ArrayList<Property> = ArrayList()
                for (i in document.documents){
                    val property = i.toObject(Property::class.java)!!
                    property.id = i.id
                    propertyList.add(property)
                }
                activity.populateBoardsListToUI(propertyList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error getting the property list", it)
            }
    }

    //Meeting Database

    fun createMeeting(activity: CreateMeetingActivity, meetingInfo : Meeting){    //register meeting into the Firestore database
        mFireStore.collection("Meeting")  //Meeting is the name of database
            .document()
            .set(meetingInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Meeting added successfully")
                Toast.makeText(activity,"Meeting added successfully", Toast.LENGTH_SHORT).show()
                activity.meetingCreatedSuccessfully()
            }.addOnFailureListener{
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error adding meeting", it)
                Toast.makeText(activity,"Meeting added successfully", Toast.LENGTH_SHORT).show()
            }
    }

    fun getUserMeetingList(activity: MyMeetingActivity){
        mFireStore.collection("Meeting")
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val meetingList: ArrayList<Meeting> = ArrayList()
                for (i in document.documents){
                    val meeting = i.toObject(Meeting::class.java)!!
                    meeting.id = i.id
                    meetingList.add(meeting)
                }
                activity.hideProgressDialog()
                activity.populateBoardsListToUI(meetingList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error getting the property list", it)
            }
    }

    fun loadCurrentUserData(activity: Activity){
        mFireStore.collection("Users")
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                when (activity){
                    is CreateMeetingActivity -> {
                        activity.CurrentUserDetails(document.toObject(User::class.java)!!)
                    }
                }

            }
    }
}