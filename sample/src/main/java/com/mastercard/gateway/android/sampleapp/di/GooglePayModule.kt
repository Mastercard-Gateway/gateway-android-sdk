package com.mastercard.gateway.android.sampleapp.di

import android.content.Context
import com.mastercard.gateway.android.sampleapp.utils.PaymentsClientWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class) // Not SingletonComponent!
object GooglePayModule {

    @Provides
    fun providePaymentsClientWrapper(@ActivityContext context: Context): PaymentsClientWrapper {
        return PaymentsClientWrapper(context);
    }
}