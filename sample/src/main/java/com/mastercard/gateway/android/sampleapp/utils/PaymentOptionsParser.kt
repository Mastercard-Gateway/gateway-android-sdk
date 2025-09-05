package com.mastercard.gateway.android.sampleapp.utils

import com.mastercard.gateway.android.sdk.GatewayMap

object PaymentOptionsParser {

    object Constants {
        const val CARD = "CARD"
    }

    @JvmStatic
    fun extractTypes(options: GatewayMap?): List<String> {
        val result = options?.get("result") as? String ?: return emptyList()
        if (!result.equals("SUCCESS", ignoreCase = true)) return emptyList()

        val paymentTypes: Map<*, *> = when (val pt = options["paymentTypes"]) {
            is GatewayMap -> pt
            is Map<*, *> -> pt
            else -> return emptyList()
        }

        val out = linkedSetOf<String>()

        if (paymentTypes["card"] != null) out += Constants.CARD

        (paymentTypes["browserPayment"] as? List<*>)?.forEach { item ->
            val type = when (item) {
                is Map<*, *> -> item["type"] as? String
                is GatewayMap -> item["type"] as? String
                else -> null
            }
            if (!type.isNullOrBlank()) out += type
        }

        return out.toList()
    }
}