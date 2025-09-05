package com.mastercard.gateway.android.sampleapp.utils

import android.view.View

object Ui {

    @JvmStatic
    fun show(v: View?) {
        v?.visibility = View.VISIBLE
    }

    @JvmStatic
    fun hide(v: View?) {
        v?.visibility = View.GONE
    }
}
