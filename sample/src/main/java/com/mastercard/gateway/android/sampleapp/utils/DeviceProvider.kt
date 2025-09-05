package com.mastercard.gateway.android.sampleapp.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider  @Inject constructor(
    @ApplicationContext private val context: Context
){

    @RequiresApi(Build.VERSION_CODES.N)
    fun getLanguage(): String {
        return context.resources.configuration.locales[0].language ?: ""
    }

    fun getScreenWidth(): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getTimezoneOffsetInMinutes(): Int {
        val now = Calendar.getInstance()
        val tz = TimeZone.getDefault()
        return tz.getOffset(now.timeInMillis) / 60000
    }
}
