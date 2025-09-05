package com.mastercard.gateway.android.sampleapp.utils

object PaymentOptionLabelResolver {

    fun labelFor(option: String?): String = when (option) {
        "CARD" -> "Card"
        "KNET" -> "KNET"
        "BENEFIT" -> "Benefit"
        "QPAY" -> "QPAY"
        "OMAN" -> "Oman Net"
        else -> option ?: "Pay"
    }
}
