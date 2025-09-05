package com.mastercard.gateway.android.sampleapp.utils

import android.util.Log
import com.mastercard.gateway.android.sampleapp.R
import com.mastercard.gateway.android.sampleapp.databinding.ActivityProcessPaymentBinding
import com.mastercard.gateway.android.sampleapp.utils.Ui.hide
import com.mastercard.gateway.android.sampleapp.utils.Ui.show
import com.mastercard.gateway.android.sdk.Gateway
import com.mastercard.gateway.android.sdk.GatewayCallback
import com.mastercard.gateway.android.sdk.GatewayMap

class AuthAndBrowserHandler(
    private val binding: ActivityProcessPaymentBinding,
    private val resultUI: ResultUI
) {

    interface ResultUI {
        fun showResult(iconRes: Int, message: String)
        fun showResult(iconRes: Int, textRes: Int)
        fun start3DS(redirectHtml: String)
        fun startBrowserPayment(redirectHtml: String)
        fun onReadyToConfirm()
    }

    private var skip3ds: Boolean = false

    fun setSkip3ds(skip: Boolean) {
        skip3ds = skip
    }

    fun isSkip3ds(): Boolean {
        return skip3ds
    }

    fun handleAuthentication(resp: GatewayMap) {
        try {
            val rec = resp["response.gatewayRecommendation"] as? String
            val status = resp["transaction.authenticationStatus"] as? String
            val redirectHtml = resp["authentication.redirect.html"] as? String

            if (rec == null) {
                show(binding.initiateAuthenticationError)
                resultUI.showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed)
                return
            }

            if (rec.equals("DO_NOT_PROCEED", true)
                || rec.equals("RESUBMIT_WITH_ALTERNATIVE_PAYMENT_DETAILS", true)
                || rec.equals("DO_NOT_PROCEED_ABANDON_ORDER", true)
            ) {
                show(binding.initiateAuthenticationError)
                resultUI.showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed)
                return
            }

            if (rec.equals("PROCEED", true)) {
                show(binding.initiateAuthenticationSuccess)
                val clean = status?.replace("AUTHENTICATION_", "")?.uppercase() ?: ""
                when (clean) {
                    "PENDING", "AVAILABLE" -> {
                        if (redirectHtml != null) {
                            show(binding.authenticateSecurePaymentLabel)
                            show(binding.authenticateSecurePaymentProgress)
                            resultUI.start3DS(redirectHtml)
                            return
                        }
                    }

                    "EXEMPT", "SUCCESSFUL", "SUCCESS", "ATTEMPTED", "NOT_SUPPORTED" -> {
                        skip3ds = true
                        resultUI.onReadyToConfirm()
                    }

                    "FAILURE" -> {
                        hide(binding.initiateAuthenticationSuccess)
                        hide(binding.initiateAuthenticationProgress)
                        show(binding.initiateAuthenticationError)
                        resultUI.showResult(
                            R.drawable.failed,
                            R.string.pay_error_3ds_authentication_failed
                        )
                    }

                    else -> {
                        if (redirectHtml != null) {
                            resultUI.start3DS(redirectHtml)
                            return
                        }
                    }
                }
            } else {
                show(binding.initiateAuthenticationError)
                resultUI.showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed)
            }

        } catch (e: Exception) {
            Log.e("AuthAndBrowserHandler", "Auth parse error", e)
            show(binding.initiateAuthenticationError)
            resultUI.showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed)
        }
    }

    fun handleBrowserPayment(result: String?, gatewayCode: String?) {
        hide(binding.authenticateBrowserPaymentProgress)

        if (result.isNullOrBlank()) {
            show(binding.authenticateBrowserPaymentError)
            resultUI.showResult(
                R.drawable.failed,
                "Browser Payment Authentication Failed"
            )
            return
        }

        if (result.equals("SUCCESS", ignoreCase = true)) {
            show(binding.authenticateBrowserPaymentSuccess)

            val message = "Browser Payment ${gatewayCode.orEmpty()}"
            resultUI.showResult(R.drawable.success, message)
            return
        }

        val message = "Browser Payment Authentication ${gatewayCode.orEmpty()}"

        when (gatewayCode.orEmpty().uppercase()) {
            "DECLINED" -> {
                show(binding.authenticateBrowserPaymentError)
                resultUI.showResult(R.drawable.failed, message)
            }

            "CANCELLED" -> {
                show(binding.authenticateBrowserPaymentError)
                resultUI.showResult(R.drawable.failed, message)
            }

            "PENDING" -> {
                show(binding.authenticateBrowserPaymentError)
                resultUI.showResult(R.drawable.failed, message)
            }

            else -> {
                show(binding.authenticateBrowserPaymentError)
                resultUI.showResult(
                    R.drawable.failed,
                    "Browser Payment Authentication Failed"
                )
            }
        }
    }

    fun makeGatewayCallback(): GatewayCallback = object : GatewayCallback {
        override fun onCancel(requestCode: Int) {
            if (requestCode == Gateway.REQUEST_BROWSER_PAYMENT) {
                hide(binding.check3dsProgress)
                show(binding.check3dsSuccess)
                hide(binding.authenticateBrowserPaymentProgress)
                show(binding.authenticateBrowserPaymentError)
                resultUI.showResult(
                    R.drawable.failed,
                    R.string.pay_error_browser_payment_authentication_cancelled
                )
            } else if (requestCode == Gateway.REQUEST_3D_SECURE) {
                hide(binding.authenticateSecurePaymentProgress)
                show(binding.authenticateSecurePaymentSuccess)
                show(binding.processPaymentLabel)
                show(binding.processPaymentError)
                resultUI.showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed)
            }
        }

        override fun onComplete(result: GatewayMap, requestCode: Int) {
            if (requestCode == Gateway.REQUEST_BROWSER_PAYMENT) {
                val status = result["result"] as? String
                val code = result["response.gatewayCode"] as? String
                handleBrowserPayment(status, code)
            } else if (requestCode == Gateway.REQUEST_3D_SECURE) {
                hide(binding.authenticateSecurePaymentProgress)
                val s = result["order.authenticationStatus"] as? String
                if (s == null) {
                    show(binding.processPaymentError)
                    resultUI.showResult(
                        R.drawable.failed,
                        R.string.pay_error_3ds_authentication_failed
                    )
                    return
                }
                when (s) {
                    "AUTHENTICATION_SUCCESSFUL" -> {
                        show(binding.authenticateSecurePaymentSuccess); resultUI.onReadyToConfirm()
                    }

                    "AUTHENTICATION_FAILED" -> {
                        show(binding.authenticateSecurePaymentError); resultUI.showResult(
                            R.drawable.failed,
                            R.string.pay_error_3ds_authentication_failed
                        )
                    }

                    "AUTHENTICATION_UNAVAILABLE" -> {
                        show(binding.authenticateSecurePaymentError); resultUI.showResult(
                            R.drawable.failed,
                            R.string.pay_error_3ds_authentication_unavailable
                        )
                    }

                    "AUTHENTICATION_REJECTED" -> {
                        show(binding.authenticateSecurePaymentError); resultUI.showResult(
                            R.drawable.failed,
                            R.string.pay_error_3ds_authentication_rejected
                        )
                    }

                    "AUTHENTICATION_PENDING" -> {
                        show(binding.authenticateSecurePaymentError); resultUI.showResult(
                            R.drawable.failed,
                            R.string.pay_error_3ds_authentication_pending
                        )
                    }

                    else -> {
                        show(binding.authenticateSecurePaymentError); resultUI.showResult(
                            R.drawable.failed,
                            R.string.pay_error_3ds_authentication_failed
                        )
                    }
                }
            }
        }
    }
}