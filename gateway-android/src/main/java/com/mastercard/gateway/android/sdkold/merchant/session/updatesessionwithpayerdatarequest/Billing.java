
package com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest;

import com.mastercard.gateway.android.sdkold.Payload;
import com.mastercard.gateway.android.sdkold.ValidationException;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.billing.Address;

public class Billing
    extends Payload
{

    private final static long serialVersionUID = 1;
    private Address address;

    @Override
    public void validate()
        throws ValidationException
    {
        if (address!= null) {
            address.validate();
        }
    }

    @Override
    public Billing readOnly(boolean b) {
        super.readOnly(b);
        if (address!= null) {
            address.readOnly(b);
        }
        return this;
    }

    public Address address() {
        if (address == null) {
            if (readOnly()) {
                return new Address().readOnly(true);
            }
            address = new Address();
            address.__setParent(this);
        }
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
        this.address.__setParent(this);
    }

    public Address getAddress() {
        return this.address;
    }

}
