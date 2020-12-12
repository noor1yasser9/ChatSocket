package com.nurbk.ps.demochat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(
    var id: String = "",
    var name: String = "",
    var image: String = "",
    var userGroup: List<String> = arrayListOf()
) : Parcelable {

    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val IMAGE = "image"
        const val USER_GROUP = "userGroup"
    }
}