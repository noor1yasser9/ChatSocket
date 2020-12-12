package com.nurbk.ps.demochat.other

import android.content.Context
import android.content.SharedPreferences

class ConfigUser private constructor(context: Context) {

    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    companion object {
        private var instance: ConfigUser? = null
        fun getInstance(context: Context): ConfigUser? {
            if (instance == null) instance = ConfigUser(context)
            return instance
        }

    }

    init {
        preferences = context.getSharedPreferences(NAME_FILE_PREF, Context.MODE_PRIVATE)
        editor = preferences!!.edit()
    }


    fun getPreferences() = preferences
    fun getEditor() = editor

}