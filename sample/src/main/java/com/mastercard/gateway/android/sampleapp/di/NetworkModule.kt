package com.mastercard.gateway.android.sampleapp.di

import com.mastercard.gateway.android.sampleapp.api.MerchantService
import com.mastercard.gateway.android.sampleapp.repo.PrefsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val timeoutValue = 500L
        val httpInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(httpInterceptor)
            .readTimeout(timeoutValue, TimeUnit.SECONDS)
            .connectTimeout(timeoutValue, TimeUnit.SECONDS)
            .writeTimeout(timeoutValue, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMerchantRetrofit(
        prefsRepository: PrefsRepository,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val merchantUrl = prefsRepository.getServerUrl()
        return Retrofit.Builder()
            .baseUrl(merchantUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMerchantService(retrofit: Retrofit): MerchantService {
        return retrofit.create(MerchantService::class.java)
    }
}