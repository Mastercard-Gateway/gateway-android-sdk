
package com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest.shipping;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;

public class Address
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String city;
    private String company;
    private String country;
    private String postcodeZip;
    private String stateProvince;
    private String street;
    private String street2;

    @Override
    public void validate()
        throws ValidationException
    {
        // no mandatory fields
    }

    @Override
    public Address readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    public String city() {
        return city;
    }

    public Address city(String city) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.city = city;
        }
        return this;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String company() {
        return company;
    }

    public Address company(String company) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.company = company;
        }
        return this;
    }

    public String getCompany() {
        return this.company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String country() {
        return country;
    }

    public Address country(String country) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.country = country;
        }
        return this;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String postcodeZip() {
        return postcodeZip;
    }

    public Address postcodeZip(String postcodeZip) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.postcodeZip = postcodeZip;
        }
        return this;
    }

    public String getPostcodeZip() {
        return this.postcodeZip;
    }

    public void setPostcodeZip(String postcodeZip) {
        this.postcodeZip = postcodeZip;
    }

    public String stateProvince() {
        return stateProvince;
    }

    public Address stateProvince(String stateProvince) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.stateProvince = stateProvince;
        }
        return this;
    }

    public String getStateProvince() {
        return this.stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String street() {
        return street;
    }

    public Address street(String street) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.street = street;
        }
        return this;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String street2() {
        return street2;
    }

    public Address street2(String street2) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.street2 = street2;
        }
        return this;
    }

    public String getStreet2() {
        return this.street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

}
