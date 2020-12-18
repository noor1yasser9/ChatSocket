package com.nurbk.ps.demochat.network

import android.content.Context
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket

class SocketManager private constructor(private var context: Context) {

    private var socket: Socket? = null
    private val BASE_URL = "http://10.0.0.1:5000"

    companion object {
        var instance: SocketManager? = null
        fun getInstance(context: Context): SocketManager? {
            if (instance == null) {
                instance = SocketManager(context)
            }
            return instance
        }
    }


    fun getSocket() = socket

    init {
        socket = IO.socket(BASE_URL)
        socket!!.connect()
    }


}