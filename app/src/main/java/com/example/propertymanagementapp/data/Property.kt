package com.example.propertymanagementapp.data

import android.os.Parcel
import android.os.Parcelable

data class Property(
    var id: String= "",
    var status: String= "",
    val userid: String = "",
    val name: String = "",
    val createdBy: String = "",
    val image: String= "",
    val description: String= "",
    val rooms: Long = 0,
    val address: String= "",
    val area: Long = 0,
    val price: Long = 0,
    val latitude: Double= 0.0,
    val longitude: Double= 0.0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readString()!!,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readValue(Long::class.java.classLoader) as Double,
        parcel.readValue(Long::class.java.classLoader) as Double
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(status)
        parcel.writeString(userid)
        parcel.writeString(name)
        parcel.writeString(createdBy)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeLong(rooms)
        parcel.writeString(address)
        parcel.writeLong(area)
        parcel.writeLong(price)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Property> {
        override fun createFromParcel(parcel: Parcel): Property {
            return Property(parcel)
        }

        override fun newArray(size: Int): Array<Property?> {
            return arrayOfNulls(size)
        }
    }
}
