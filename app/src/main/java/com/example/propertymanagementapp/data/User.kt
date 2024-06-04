package com.example.propertymanagementapp.data

import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: String= "",
    val name: String= "",
    val email: String= "",
    val image: String= "", //image is nullable
    val mobileNumber: Long = 0, //mobileNumber is nullable
    val fcmtoken: String = ""    //to know that it is a specific user that is logged in
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(image)
        parcel.writeValue(mobileNumber)
        parcel.writeString(fcmtoken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
