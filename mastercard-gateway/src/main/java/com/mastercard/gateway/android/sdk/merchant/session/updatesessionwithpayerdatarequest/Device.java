
package com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;

public class Device
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String browser;
    private String fingerprint;
    private String hostname;
    private String ipAddress;
    private String mobilePhoneModel;

    @Override
    public void validate()
        throws ValidationException
    {
        // no mandatory fields
    }

    @Override
    public Device readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    public String browser() {
        return browser;
    }

    public Device browser(String browser) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.browser = browser;
        }
        return this;
    }

    public String getBrowser() {
        return this.browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String fingerprint() {
        return fingerprint;
    }

    public Device fingerprint(String fingerprint) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.fingerprint = fingerprint;
        }
        return this;
    }

    public String getFingerprint() {
        return this.fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String hostname() {
        return hostname;
    }

    public Device hostname(String hostname) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.hostname = hostname;
        }
        return this;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public Device ipAddress(String ipAddress) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.ipAddress = ipAddress;
        }
        return this;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String mobilePhoneModel() {
        return mobilePhoneModel;
    }

    public Device mobilePhoneModel(String mobilePhoneModel) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.mobilePhoneModel = mobilePhoneModel;
        }
        return this;
    }

    public String getMobilePhoneModel() {
        return this.mobilePhoneModel;
    }

    public void setMobilePhoneModel(String mobilePhoneModel) {
        this.mobilePhoneModel = mobilePhoneModel;
    }

}
