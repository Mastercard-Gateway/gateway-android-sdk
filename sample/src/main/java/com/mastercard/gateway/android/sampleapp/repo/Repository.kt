package com.mastercard.gateway.android.sampleapp.repo

import com.mastercard.gateway.android.sampleapp.api.MerchantService
import com.mastercard.gateway.android.sdk.GatewayMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val merchantService: MerchantService
) {

    suspend fun createSession(payload: GatewayMap): GatewayMap =
        withContext(Dispatchers.IO) {
            merchantService.createSession(payload)
        }

    suspend fun initiateAuthentication(
        orderId: String,
        transactionId: String,
        payload: GatewayMap
    ): GatewayMap =
        withContext(Dispatchers.IO) {
            merchantService.initiateAuthentication(orderId, transactionId, payload)
        }

    suspend fun submitTransaction(
        orderId: String,
        transactionId: String,
        payload: GatewayMap
    ): GatewayMap =
        withContext(Dispatchers.IO) {
            merchantService.submitTransaction(orderId, transactionId, payload)
        }

    suspend fun inquirePaymentOptions(): GatewayMap =
        withContext(Dispatchers.IO) {
            merchantService.inquirePaymentOptions()
        }

    suspend fun initiateBrowserPayment(
        orderId: String,
        transactionId: String,
        request: GatewayMap
    ): GatewayMap =
        withContext(Dispatchers.IO) {
            merchantService.initiateBrowserPayment(orderId, transactionId, request)
        }
}