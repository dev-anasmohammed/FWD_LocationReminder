package com.devanasmohammed.locationreminder.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManger {

    companion object {
        const val APP_KEY = "Location Reminder"

        private var sharedPreference: SharedPreferences? = null

        //save Boolean data
        fun saveData(context: Context, key: String, value: Boolean) {
            if (sharedPreference == null) {
                sharedPreference =
                    context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE)
            }
            val editor = sharedPreference?.edit()!!
            editor.putBoolean(key, value)
            editor.apply()
        }

        //load boolean data
        fun loadBoolean(
            context: Context,
            key: String,
            defaultVal: Boolean
        ): Boolean? {
            if (sharedPreference == null) {
                sharedPreference =
                    context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE)
            }
            return sharedPreference?.getBoolean(key, defaultVal)
        }

        @SuppressLint("CommitPrefEdits")
        fun clean(context: Context) {
            if (sharedPreference == null) {
                sharedPreference =
                    context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE)
            }
            sharedPreference?.edit()?.clear()?.apply()
        }

    }
}