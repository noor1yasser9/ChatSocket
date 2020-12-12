package com.nurbk.ps.demochat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var image: String = "",
    var password: String = "",
    var isOnline: Boolean = false
) : Parcelable {

    companion object {
        const val NAME = "name"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val ID = "id"
        const val IMAGE = "image"
        const val IS_ONLINE = "isOnline"
    }

}