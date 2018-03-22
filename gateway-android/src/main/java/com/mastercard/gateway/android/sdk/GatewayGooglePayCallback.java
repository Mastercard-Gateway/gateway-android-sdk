package com.mastercard.gateway.android.sdk;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.PaymentData;

public interface GatewayGooglePayCallback {

    /**
     * Called when payment data is returned from GooglePay
     *
     * @param paymentData An object containing details about the payment
     */
    void onReceivedPaymentData(PaymentData paymentData);

    /**
     * Called when a user cancels a GooglePay transaction
     */
    void onGooglePayCancelled();

    /**
     * Called when an error occurs during a GooglePay transaction
     *
     * @param errorCode The corresponding error code (see {@link com.google.android.gms.wallet.WalletConstants} for a list of supported errors)
     */
    void onGooglePayError(Status status);
}
