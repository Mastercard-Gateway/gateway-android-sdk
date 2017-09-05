
package com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest;

import com.mastercard.gateway.android.sdkold.Payload;
import com.mastercard.gateway.android.sdkold.ValidationException;

public class Customer
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String email;
    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String phone;

    @Override
    public void validate()
        throws ValidationException
    {
        // no mandatory fields
    }

    @Override
    public Customer readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    public String email() {
        return email;
    }

    public Customer email(String email) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.email = email;
        }
        return this;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String firstName() {
        return firstName;
    }

    public Customer firstName(String firstName) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.firstName = firstName;
        }
        return this;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String lastName() {
        return lastName;
    }

    public Customer lastName(String lastName) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.lastName = lastName;
        }
        return this;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String mobilePhone() {
        return mobilePhone;
    }

    public Customer mobilePhone(String mobilePhone) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.mobilePhone = mobilePhone;
        }
        return this;
    }

    public String getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String phone() {
        return phone;
    }

    public Customer phone(String phone) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.phone = phone;
        }
        return this;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
