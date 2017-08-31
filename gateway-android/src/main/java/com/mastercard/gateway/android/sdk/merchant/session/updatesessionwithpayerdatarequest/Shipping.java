
package com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;
import com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest.shipping.Address;
import com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest.shipping.Contact;

public class Shipping
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String method;
    private Address address;
    private Contact contact;

    @Override
    public void validate()
        throws ValidationException
    {
        if (address!= null) {
            address.validate();
        }
        if (contact!= null) {
            contact.validate();
        }
    }

    @Override
    public Shipping readOnly(boolean b) {
        super.readOnly(b);
        if (address!= null) {
            address.readOnly(b);
        }
        if (contact!= null) {
            contact.readOnly(b);
        }
        return this;
    }

    public String method() {
        return method;
    }

    public Shipping method(String method) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.method = method;
        }
        return this;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public Contact contact() {
        if (contact == null) {
            if (readOnly()) {
                return new Contact().readOnly(true);
            }
            contact = new Contact();
            contact.__setParent(this);
        }
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
        this.contact.__setParent(this);
    }

    public Contact getContact() {
        return this.contact;
    }

}
