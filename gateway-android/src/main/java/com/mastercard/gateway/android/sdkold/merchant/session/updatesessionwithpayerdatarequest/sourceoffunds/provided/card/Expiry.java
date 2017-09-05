
package com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds.provided.card;

import com.mastercard.gateway.android.sdkold.Payload;
import com.mastercard.gateway.android.sdkold.ValidationException;

public class Expiry
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String month;
    private String year;

    @Override
    public void validate()
        throws ValidationException
    {
        // no mandatory fields
    }

    @Override
    public Expiry readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    public String month() {
        return month;
    }

    public Expiry month(String month) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.month = month;
        }
        return this;
    }

    public String getMonth() {
        return this.month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String year() {
        return year;
    }

    public Expiry year(String year) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.year = year;
        }
        return this;
    }

    public String getYear() {
        return this.year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
