package com.mastercard.gateway.android.sampleapp.repo

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepository @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "app_prefs"

        private const val KEY_MERCHANT_ID = "merchant_id"
        private const val KEY_REGION = "region"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PAYMENT_TYPE = "payment_type"

        private const val DEFAULT_STRING = ""
    }

    fun saveMerchantId(merchantId: String) = prefs.edit { putString(KEY_MERCHANT_ID, merchantId) }
    fun saveRegion(region: String) = prefs.edit { putString(KEY_REGION, region) }
    fun saveServerUrl(url: String) = prefs.edit { putString(KEY_SERVER_URL, url) }
    fun savePaymentType(type: String) = prefs.edit { putString(KEY_PAYMENT_TYPE, type) }
    fun getMerchantId(): String = prefs.getString(KEY_MERCHANT_ID, DEFAULT_STRING) ?: DEFAULT_STRING
    fun getRegion(): String = prefs.getString(KEY_REGION, DEFAULT_STRING) ?: DEFAULT_STRING
    fun getServerUrl(): String = prefs.getString(KEY_SERVER_URL, DEFAULT_STRING) ?: DEFAULT_STRING
    fun getPaymentType(): String = prefs.getString(KEY_PAYMENT_TYPE, DEFAULT_STRING) ?: DEFAULT_STRING

    private inline fun SharedPreferences.edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        val editor = edit()
        action(editor)
        if (commit) editor.commit() else editor.apply()
    }
}