
package com.mastercard.gateway.android.sdkold;


public interface Comms {


    public String send(String method, String url, String request)
        throws CommsException
    ;

}
