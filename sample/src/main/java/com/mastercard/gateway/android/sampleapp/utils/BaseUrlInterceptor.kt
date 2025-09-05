package com.mastercard.gateway.android.sampleapp.utils

import android.util.Log
import com.mastercard.gateway.android.sampleapp.repo.PrefsRepository
import com.mastercard.gateway.android.sampleapp.viewmodel.ProcessPaymentViewModel
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject

class BaseUrlInterceptor @Inject constructor(
    private val prefsRepository: PrefsRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Fetch latest user-configurable values
        val region = prefsRepository.getRegion()
        val version = ProcessPaymentViewModel.API_VERSION

        val newBaseUrl = HttpUrl.Builder()
            .scheme("https")
            .host("${region.lowercase(Locale.ROOT)}.gateway.mastercard.com")
            .addPathSegment("api")
            .addPathSegment("rest")
            .addPathSegment("version")
            .addPathSegment(version)
            .build()

        // Extract the remaining path after baseUrl
        val newFullUrl = newBaseUrl.newBuilder()
        for (segment in originalUrl.pathSegments) {
            newFullUrl.addPathSegment(segment)
        }

        val updatedRequest = originalRequest.newBuilder()
            .url(newFullUrl.build())
            .build()

        Log.e("URL_INTERCEPTOR", "Final URL: ${updatedRequest.url}")

        return chain.proceed(updatedRequest)
    }

}