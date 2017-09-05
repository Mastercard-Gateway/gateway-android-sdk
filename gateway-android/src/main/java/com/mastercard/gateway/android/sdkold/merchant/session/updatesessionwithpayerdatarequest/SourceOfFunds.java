
package com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest;

import com.mastercard.gateway.android.sdkold.Payload;
import com.mastercard.gateway.android.sdkold.ValidationException;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds.Provided;

public class SourceOfFunds
    extends Payload
{

    private final static long serialVersionUID = 1;
    private Provided provided;

    @Override
    public void validate()
        throws ValidationException
    {
        if (provided!= null) {
            provided.validate();
        }
    }

    @Override
    public SourceOfFunds readOnly(boolean b) {
        super.readOnly(b);
        if (provided!= null) {
            provided.readOnly(b);
        }
        return this;
    }

    public Provided provided() {
        if (provided == null) {
            if (readOnly()) {
                return new Provided().readOnly(true);
            }
            provided = new Provided();
            provided.__setParent(this);
        }
        return provided;
    }

    public void setProvided(Provided provided) {
        this.provided = provided;
        this.provided.__setParent(this);
    }

    public Provided getProvided() {
        return this.provided;
    }

}
