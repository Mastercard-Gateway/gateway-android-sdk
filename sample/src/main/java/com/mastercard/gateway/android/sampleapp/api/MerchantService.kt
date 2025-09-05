package com.mastercard.gateway.android.sampleapp.api

import androidx.annotation.Keep
import com.mastercard.gateway.android.sdk.GatewayMap
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

@Keep
@JvmSuppressWildcards
interface MerchantService {

    @POST(CREATE_SESSION_ENDPOINT)
    suspend fun createSession(@Body payload: GatewayMap): GatewayMap
    @POST(PAYMENT_OPTIONS_INQUIRY_ENDPOINT)
    suspend fun inquirePaymentOptions(): GatewayMap

    @PUT(START_AUTHENTICATION_ENDPOINT)
    suspend fun initiateAuthentication(
        @Query(ORDER_ID_PARAM) orderId: String,
        @Query(TRANSACTION_ID_PARAM) transactionId: String,
        @Body payload: GatewayMap
    ): GatewayMap

    @PUT(START_BROWSER_PAYMENT_ENDPOINT)
    suspend fun initiateBrowserPayment(
        @Query(ORDER_ID_PARAM) orderId: String,
        @Query(TRANSACTION_ID_PARAM) transactionId: String,
        @Body payload: GatewayMap
    ): GatewayMap

    @PUT(SUBMIT_TRANSACTION_ENDPOINT)
    suspend fun submitTransaction(
        @Query(ORDER_ID_PARAM) orderId: String,
        @Query(TRANSACTION_ID_PARAM) transactionId: String,
        @Body payload: GatewayMap
    ): GatewayMap

    companion object {
        // Query keys (keep consistent & reusable)
        const val ORDER_ID_PARAM = "orderId"
        const val TRANSACTION_ID_PARAM = "transactionId"

        // Endpoints
        const val CREATE_SESSION_ENDPOINT = "session.php"
        const val SUBMIT_TRANSACTION_ENDPOINT = "transaction.php"
        const val START_AUTHENTICATION_ENDPOINT = "start-authentication.php"

        const val PAYMENT_OPTIONS_INQUIRY_ENDPOINT = "payment-options-inquiry.php"
        const val START_BROWSER_PAYMENT_ENDPOINT = "start-browser-payment.php"
    }
}