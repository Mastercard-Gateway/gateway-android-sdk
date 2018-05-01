package com.mastercard.gateway.android.sampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mastercard.gateway.android.sdk.Gateway;

public enum Config {

    MERCHANT_ID(""),
    REGION(Gateway.Region.NORTH_AMERICA.name()),
    MERCHANT_URL("");

    String defValue;

    Config(String defValue) {
        this.defValue = defValue;
    }

    public String getValue(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(this.name(), defValue);
    }

    public void setValue(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(this.name(), value);
        editor.apply();
    }
}
