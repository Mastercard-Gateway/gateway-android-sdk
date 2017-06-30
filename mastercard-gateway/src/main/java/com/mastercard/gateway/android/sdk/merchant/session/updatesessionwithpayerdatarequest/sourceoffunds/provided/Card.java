
package com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds.provided;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;
import com.mastercard.gateway.android.sdk.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds.provided.card.Expiry;

public class Card
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String nameOnCard;
    private String number;
    private String securityCode;
    private Expiry expiry;

    @Override
    public void validate()
        throws ValidationException
    {
        if (expiry!= null) {
            expiry.validate();
        }
    }

    @Override
    public Card readOnly(boolean b) {
        super.readOnly(b);
        if (expiry!= null) {
            expiry.readOnly(b);
        }
        return this;
    }

    public String nameOnCard() {
        return nameOnCard;
    }

    public Card nameOnCard(String nameOnCard) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.nameOnCard = nameOnCard;
        }
        return this;
    }

    public String getNameOnCard() {
        return this.nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public String number() {
        return number;
    }

    public Card number(String number) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.number = number;
        }
        return this;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String securityCode() {
        return securityCode;
    }

    public Card securityCode(String securityCode) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.securityCode = securityCode;
        }
        return this;
    }

    public String getSecurityCode() {
        return this.securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public Expiry expiry() {
        if (expiry == null) {
            if (readOnly()) {
                return new Expiry().readOnly(true);
            }
            expiry = new Expiry();
            expiry.__setParent(this);
        }
        return expiry;
    }

    public void setExpiry(Expiry expiry) {
        this.expiry = expiry;
        this.expiry.__setParent(this);
    }

    public Expiry getExpiry() {
        return this.expiry;
    }

}
