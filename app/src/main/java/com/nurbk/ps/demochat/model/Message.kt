package com.nurbk.ps.demochat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "MessageTable")
data class Message(
    var username: String = "",
    var message: String = "",
    var imageUser: String = "",
    var idS: String = "",
    var isTyping: Boolean = false,
    var idUser: String = "",
    var time: String = "",
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
) {


    companion object {
        const val USERNAME = "username"
        const val MESSAGE = "message"
        const val ID = "idS"
        const val IMAGE_USER = "imageUser"
        const val TYPING = "isTyping"
    }
}
