
package com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;

public class Session
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String version;

    @Override
    public void validate()
        throws ValidationException
    {
        // no mandatory fields
    }

    @Override
    public Session readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    public String version() {
        return version;
    }

    public Session version(String version) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.version = version;
        }
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
