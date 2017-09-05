package com.mastercard.gateway.android.sdkold;

/**
 * A communications error in an SSL request, due to timeout
 */
public class CommsTimeoutException extends CommsException {
    public CommsTimeoutException( String message ) {
        super( message );
    }
}
