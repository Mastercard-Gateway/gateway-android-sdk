package com.mastercard.gateway.android.sampleapp;


class MerchantSimulatorResponse {
    String message;
    String sessionID;
    String status;
    String encryptionToken;

    public String toString (){
        String repr = "message: " + message + ", sessionId: " + sessionID + ", status:" + status;

        if ( encryptionToken != null ) {
            repr += ", encryptionToken: " + encryptionToken;
        }

        return "SDKResponse [ " + repr + " ]";
    }
}
