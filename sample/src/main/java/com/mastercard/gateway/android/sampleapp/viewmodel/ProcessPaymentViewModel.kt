package com.mastercard.gateway.android.sampleapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mastercard.gateway.android.sampleapp.repo.PrefsRepository
import com.mastercard.gateway.android.sampleapp.repo.Repository
import com.mastercard.gateway.android.sampleapp.utils.DeviceInfoProvider
import com.mastercard.gateway.android.sdk.Gateway
import com.mastercard.gateway.android.sdk.GatewayCallback
import com.mastercard.gateway.android.sdk.GatewayMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class GatewayResult<out T> {
    data class Success<out T>(val response: T) : GatewayResult<T>()
    data class Error(val throwable: Throwable) : GatewayResult<Nothing>()
}

typealias CreateSessionResult = GatewayResult<GatewayMap>
typealias UpdateSessionResult = GatewayResult<GatewayMap>
typealias TransactionResult = GatewayResult<GatewayMap>
typealias InitiateAuthentication = GatewayResult<GatewayMap>
typealias CompleteSessionResult = GatewayResult<String>
typealias BrowserPaymentResult = GatewayResult<GatewayMap>
typealias PaymentOptionsInquiryResult = GatewayResult<GatewayMap>

@HiltViewModel
class ProcessPaymentViewModel @Inject constructor(
    private val repository: Repository,
    private val prefsRepo: PrefsRepository,
    private val deviceInfoProvider: DeviceInfoProvider
) : ViewModel() {

    private val _sessionResult = MutableLiveData<CreateSessionResult>()
    val sessionResult: LiveData<CreateSessionResult> = _sessionResult

    private val _paymentOptionsInquiryResult = MutableLiveData<PaymentOptionsInquiryResult>()
    val paymentOptionsInquiryResult: LiveData<PaymentOptionsInquiryResult> = _paymentOptionsInquiryResult

    private val _transactionResult = MutableLiveData<TransactionResult>()
    val transactionResult: LiveData<TransactionResult> = _transactionResult

    private val _startAuthenticationResult = MutableLiveData<InitiateAuthentication>()
    val startAuthenticationResult: LiveData<InitiateAuthentication> = _startAuthenticationResult

    private val _completeSessionResult = MutableLiveData<CompleteSessionResult>()
    val completeSessionResult: LiveData<CompleteSessionResult> = _completeSessionResult

    private val _updateSessionResult = MutableLiveData<UpdateSessionResult>()
    val updateSessionResult: LiveData<UpdateSessionResult> = _updateSessionResult

    private val _browserPaymentResult = MutableLiveData<BrowserPaymentResult>()
    val browserPaymentResult: LiveData<BrowserPaymentResult> = _browserPaymentResult

    private val merchantId = prefsRepo.getMerchantId()
    private val region = prefsRepo.getRegion()

    var gateway: Gateway = Gateway()

    var sessionId = ""

    // random order/txn IDs for example purposes
    var orderId = UUID.randomUUID().toString().substringBefore("-")
    val transactionId = UUID.randomUUID().toString().substringBefore("-")
    val threeDSecureId: String? = null

    private var paymentOptionsResponse: GatewayMap? = null

    var CURRENCY = "USD"

    init {
        gateway.setMerchantId(merchantId)
        gateway.setRegion(Gateway.Region.valueOf(region))
    }

    fun createSession(request: GatewayMap) {
        viewModelScope.launch {
            try {
                val result = repository.createSession(request)
                val status = result.getString("result")

                if (status == "SUCCESS") {
                    sessionId = result.getString("session.id") // correct path for session id
                    _sessionResult.postValue(GatewayResult.Success(result))
                } else {
                    _sessionResult.postValue(
                        GatewayResult.Error(Exception("CreateSession failed"))
                    )
                }
            } catch (e: Exception) {
                _sessionResult.postValue(GatewayResult.Error(e))
            }
        }
    }

    fun inquirePaymentOptions() {
        viewModelScope.launch {
            try {
                val resp = repository.inquirePaymentOptions()
                val status = resp.getString("result") ?: "UNKNOWN"

                if (status == "SUCCESS") {
                    paymentOptionsResponse = resp
                    _paymentOptionsInquiryResult.postValue(
                        GatewayResult.Success(resp)
                    )
                } else {
                    _paymentOptionsInquiryResult.postValue(
                        GatewayResult.Error(Exception("PaymentOptions inquiry failed:"))
                    )
                }
            } catch (e: Exception) {
                _paymentOptionsInquiryResult.postValue(GatewayResult.Error(e))
            }
        }
    }

    private fun GatewayMap.getString(path: String): String =
        (this[path] as? String).orEmpty()

    fun saveCurrencyForSelectedFlow(type: String) {
        val map = paymentOptionsResponse ?: return
        when (type) {
            CARD_PAYMENT_OPTION -> {
                val cardCurrency = map.getString("paymentTypes.card.currencies[0].currency")
                if (cardCurrency.isNotBlank()) {
                    CURRENCY = cardCurrency
                }
            }
            KNET_PAYMENT_OPTION, BENEFIT_PAYMENT_OPTION, QPAY_PAYMENT_OPTION, OMAN_PAYMENT_OPTION -> {
                val browserCurrency = map.getString("paymentTypes.browserPayment[0].currencies[0].currency")
                val browserType = map.getString("paymentTypes.browserPayment[0].type")
                if (browserCurrency.isNotBlank()) {
                    CURRENCY = browserCurrency
                }
                if (browserType.isNotBlank()) {
                    prefsRepo.savePaymentType(browserType)
                }
            }
            else -> Unit
        }
    }

    fun updateSession(
        paymentToken: String?,
        number: String = "",
        expiryMonth: String = "",
        expiryYear: String = "",
        cvv: String = ""
    ) {
        val request: GatewayMap = if (paymentToken != null) {
            GatewayMap()
                .set("sourceOfFunds.provided.card.devicePayment.paymentToken", paymentToken)
        } else {

            GatewayMap()
                .set("sourceOfFunds.type", "CARD")
                .set("sourceOfFunds.provided.card.number", number)
                .set("sourceOfFunds.provided.card.expiry.month", expiryMonth)
                .set("sourceOfFunds.provided.card.expiry.year", expiryYear)
                .set("sourceOfFunds.provided.card.securityCode", cvv)
        }

        gateway.updateSession(
            sessionId,
            API_VERSION,
            request,
            object : GatewayCallback {
                override fun onSuccess(response: GatewayMap) {
                    _updateSessionResult.postValue(GatewayResult.Success(response))
                }

                override fun onError(throwable: Throwable) {
                    _updateSessionResult.postValue(GatewayResult.Error(throwable))
                }
            }
        )
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun initiateAuthentication() {
        viewModelScope.launch {
            try {
                val payload = GatewayMap()
                    .set("apiOperation", "INITIATE_AUTHENTICATION")
                    .set("session.id", sessionId)
                    .set("authentication.purpose", "PAYMENT_TRANSACTION")
                    .set("authentication.channel", "PAYER_BROWSER")
                    .set("order.currency", CURRENCY)
                    .set("order.amount", AMOUNT)
                    .set("device.browser", "ANDROID_WEB_VIEW")
                    .set("device.browserDetails.3DSecureChallengeWindowSize", "FULL_SCREEN")
                    .set("device.browserDetails.acceptHeaders", "application/json")
                    .set("device.browserDetails.colorDepth", 24)
                    .set("device.browserDetails.javaEnabled", false)
                    .set("device.browserDetails.language", deviceInfoProvider.getLanguage())
                    .set("device.browserDetails.screenHeight", deviceInfoProvider.getScreenHeight())
                    .set("device.browserDetails.screenWidth", deviceInfoProvider.getScreenWidth())
                    .set("device.browserDetails.timeZone", deviceInfoProvider.getTimezoneOffsetInMinutes())

                val result = repository.initiateAuthentication(
                    orderId = orderId,
                    transactionId = transactionId,
                    payload = payload
                )
                _startAuthenticationResult.postValue(GatewayResult.Success(result))
            } catch (e: Exception) {
                _startAuthenticationResult.postValue(GatewayResult.Error(e))
            }
        }
    }

    fun submitTransaction(isGooglePay: Boolean) {
        viewModelScope.launch {
            try {
                val completeSessionRequest = GatewayMap()
                    .set("apiOperation", "PAY")
                    .set("session.id", sessionId)
                    .set("order.amount", AMOUNT)
                    .set("order.currency", CURRENCY)
                    .apply { if (isGooglePay) set("order.walletProvider", "GOOGLE_PAY") }
                    .set("sourceOfFunds.type", "CARD")
                    .set("transaction.source", "INTERNET")
                    .set("threeDSecureId", threeDSecureId)

                repository.submitTransaction(orderId, transactionId, completeSessionRequest)
                _completeSessionResult.postValue(GatewayResult.Success("DONE"))
            } catch (e: Exception) {
                _completeSessionResult.postValue(GatewayResult.Error(e))
            }
        }
    }

    fun initiateBrowserPayment() {
        val request = GatewayMap()
            .set("apiOperation", "INITIATE_BROWSER_PAYMENT")
            .set("browserPayment.operation", "PAY")
            .set(
                "browserPayment.returnUrl",
                "https://francophone-leaf-52430-c8565a556f27.herokuapp.com/browser-payment-callback.php?order=$orderId&transaction=$transactionId"
            )
            .set("customer.phone", "1234567892")
            .set("order.amount", AMOUNT)
            .set("order.currency", CURRENCY)
            .set("sourceOfFunds.type", "BROWSER_PAYMENT")
            .set("sourceOfFunds.browserPayment.type", prefsRepo.getPaymentType())

        viewModelScope.launch {
            try {
                val result = repository.initiateBrowserPayment(orderId, transactionId, request)
                _browserPaymentResult.postValue(GatewayResult.Success(result))
            } catch (e: Exception) {
                _browserPaymentResult.postValue(GatewayResult.Error(e))
            }
        }
    }

    companion object {
        const val API_VERSION = "100"
        const val AMOUNT = "1.00"
        const val CARD_PAYMENT_OPTION: String = "card"
        const val KNET_PAYMENT_OPTION: String = "KNET"
        const val BENEFIT_PAYMENT_OPTION: String = "BENEFIT_BH"
        const val QPAY_PAYMENT_OPTION: String = "QPAY"
        const val OMAN_PAYMENT_OPTION: String = "OMAN_NET"
    }
}