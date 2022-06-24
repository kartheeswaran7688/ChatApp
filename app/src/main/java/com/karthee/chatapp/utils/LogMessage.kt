package com.karthee.chatapp.utils

import android.util.Log
import com.karthee.chatapp.BuildConfig.DEBUG

object LogMessage {

    private val logVisible = DEBUG

    internal fun v(msg: String) {
        if (logVisible) Log.v("ChatApp",msg)
    }

    internal fun e(msg: String) {
        if (logVisible) Log.e("ChatApp",msg)
    }

}