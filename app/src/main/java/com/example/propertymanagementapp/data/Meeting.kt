package com.example.propertymanagementapp.data

import android.os.Parcel
import android.os.Parcelable
import java.sql.Time

data class Meeting(
    var id: String= "",
    var status: String= "",
    val userid: String = "",
    val ownerid: String = "",
    var location: String= "",
    val latitude: Double= 0.0,
    val longitude: Double= 0.0,
    var year: Long = 0,
    var month: Long = 0,
    var day: Long = 0,
    var hour: Long = 0,
    var minute: Long = 0

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Long::class.java.classLoader) as Double,
        parcel.readValue(Long::class.java.classLoader) as Double,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readValue(Long::class.java.classLoader) as Long,
        parcel.readValue(Long::class.java.classLoader) as Long

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(status)
        parcel.writeString(userid)
        parcel.writeString(ownerid)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeLong(year)
        parcel.writeLong(month)
        parcel.writeLong(day)
        parcel.writeLong(hour)
        parcel.writeLong(minute)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meeting> {
        override fun createFromParcel(parcel: Parcel): Meeting {
            return Meeting(parcel)
        }
        override fun newArray(size: Int): Array<Meeting?> {
            return arrayOfNulls(size)
        }
    }
}
