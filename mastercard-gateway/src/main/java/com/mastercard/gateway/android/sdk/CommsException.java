
package com.mastercard.gateway.android.sdk;


public class CommsException
    extends Exception
{


    public CommsException(String message) {
        super(message);
    }

    public CommsException(String message, Throwable cause) {
        super(message, cause);
    }

}
