package com.mastercard.gateway.android.sampleapp.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.mastercard.gateway.android.sampleapp.repo.PrefsRepository
import com.mastercard.gateway.android.sampleapp.utils.PaymentsClientWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class CollectCardInfoViewModel @Inject constructor(
    private val prefsRepo: PrefsRepository
) : ViewModel() {

    private val _isGooglePayReady = MutableLiveData<GoogleCheckResult?>(null)
    val isGooglePayReady: LiveData<GoogleCheckResult?> = _isGooglePayReady

    private val _launchGooglePay = MutableLiveData<PaymentDataRequest?>()
    val launchGooglePay: LiveData<PaymentDataRequest?> = _launchGooglePay

    // --- Public API ---

    fun checkGooglePay(paymentsClientWrapper: PaymentsClientWrapper) {
        try {
            val request = buildIsReadyToPayRequest()
            val task = paymentsClientWrapper.isReadyToPay(request)
            task.addOnCompleteListener { t ->
                try {
                    val result = t.getResult(ApiException::class.java) ?: false
                    _isGooglePayReady.postValue(GoogleCheckResult.Success(result))
                } catch (e: ApiException) {
                    _isGooglePayReady.postValue(GoogleCheckResult.Error(e))
                } catch (e: Exception) {
                    _isGooglePayReady.postValue(GoogleCheckResult.Error(e))
                }
            }
        } catch (e: JSONException) {
            _isGooglePayReady.postValue(GoogleCheckResult.Error(e))
        }
    }

    fun onGooglePayButtonClicked() {
        try {
            val request = buildPaymentDataRequest(
                merchantName = "Example Merchant",
                merchantGatewayId = prefsRepo.getMerchantId(), // your MPGS gatewayMerchantId
                totalPrice = "1.00",
                currencyCode = "USD"
            )
            _launchGooglePay.value = request
        } catch (e: JSONException) {
            _launchGooglePay.value = null
        }
    }

    // --- Builders (JSONObject -> *Request) ---

    private fun baseRequest(): JSONObject = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    private fun allowedCardNetworks(): JSONArray = JSONArray()
        .put("AMEX")
        .put("DISCOVER")
        .put("MASTERCARD")
        .put("VISA")

    private fun allowedAuthMethods(): JSONArray = JSONArray()
        .put("PAN_ONLY")
        .put("CRYPTOGRAM_3DS")

    private fun baseCardPaymentMethod(): JSONObject = JSONObject()
        .put("type", "CARD")
        .put(
            "parameters", JSONObject()
                .put("allowedAuthMethods", allowedAuthMethods())
                .put("allowedCardNetworks", allowedCardNetworks())
        )

    private fun tokenizationSpecification(merchantGatewayId: String): JSONObject =
        JSONObject()
            .put("type", "PAYMENT_GATEWAY")
            .put(
                "parameters", JSONObject()
                    .put("gateway", "mpgs")
                    .put("gatewayMerchantId", merchantGatewayId)
            )

    private fun cardPaymentMethodWithTokenization(merchantGatewayId: String): JSONObject =
        baseCardPaymentMethod()
            .put("parameters",
                baseCardPaymentMethod().getJSONObject("parameters")
                    // optional but often required for address details
                    .put("billingAddressRequired", true)
                    .put("billingAddressParameters", JSONObject().put("format", "FULL"))
            )
            .put("tokenizationSpecification", tokenizationSpecification(merchantGatewayId))

    private fun transactionInfo(totalPrice: String, currencyCode: String): JSONObject =
        JSONObject()
            .put("totalPrice", totalPrice)
            .put("totalPriceStatus", "FINAL")
            .put("currencyCode", currencyCode)

    private fun merchantInfo(merchantName: String): JSONObject =
        JSONObject().put("merchantName", merchantName)

    private fun isReadyToPayRequestJson(): JSONObject =
        baseRequest().put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
            // Optional toggle; leave false for a capability check
            .put("existingPaymentMethodRequired", false)

    private fun paymentDataRequestJson(
        merchantName: String,
        merchantGatewayId: String,
        totalPrice: String,
        currencyCode: String
    ): JSONObject =
        baseRequest()
            .put("allowedPaymentMethods", JSONArray().put(cardPaymentMethodWithTokenization(merchantGatewayId)))
            .put("transactionInfo", transactionInfo(totalPrice, currencyCode))
            .put("merchantInfo", merchantInfo(merchantName))

    // --- Final Request objects ---

    private fun buildIsReadyToPayRequest(): IsReadyToPayRequest =
        IsReadyToPayRequest.fromJson(isReadyToPayRequestJson().toString())

    private fun buildPaymentDataRequest(
        merchantName: String,
        merchantGatewayId: String,
        totalPrice: String,
        currencyCode: String
    ): PaymentDataRequest =
        PaymentDataRequest.fromJson(
            paymentDataRequestJson(
                merchantName = merchantName,
                merchantGatewayId = merchantGatewayId,
                totalPrice = totalPrice,
                currencyCode = currencyCode
            ).toString()
        )

    // --- Results ---

    sealed class GoogleCheckResult {
        data class Success(val result: Boolean) : GoogleCheckResult()
        data class Error(val throwable: Throwable) : GoogleCheckResult()
    }

}