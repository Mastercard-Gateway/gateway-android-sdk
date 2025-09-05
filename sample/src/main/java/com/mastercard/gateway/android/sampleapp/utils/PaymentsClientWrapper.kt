package com.mastercard.gateway.android.sampleapp.utils

import android.content.Context
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.tasks.Task


class PaymentsClientWrapper(context: Context?) {
    val client: PaymentsClient

    init {
        val walletOptions = WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()

        this.client = Wallet.getPaymentsClient(context!!, walletOptions)
    }

    fun isReadyToPay(request: IsReadyToPayRequest?): Task<Boolean> {
        return client.isReadyToPay(request!!)
    }
}