package com.example.propertymanagementapp.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.propertymanagementapp.LoginActivities.SignInActivity
import com.example.propertymanagementapp.LoginActivities.SignUpActivity
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.mainUI.CreatePropertyActivity
import com.example.propertymanagementapp.mainUI.MainActivity
import com.example.propertymanagementapp.mainUI.MyProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    fun loadUserData(activity: Activity){
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
                            activity.updateNavigationUserDetails(loggedInUser)
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
                Log.e(activity.javaClass.simpleName, "Error while creating a board",e)
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
}