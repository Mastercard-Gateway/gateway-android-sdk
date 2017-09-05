
package com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds;

import com.mastercard.gateway.android.sdkold.Payload;
import com.mastercard.gateway.android.sdkold.ValidationException;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds.provided.Card;

public class Provided
    extends Payload
{

    private final static long serialVersionUID = 1;
    private Card card;

    @Override
    public void validate()
        throws ValidationException
    {
        if (card!= null) {
            card.validate();
        }
    }

    @Override
    public Provided readOnly(boolean b) {
        super.readOnly(b);
        if (card!= null) {
            card.readOnly(b);
        }
        return this;
    }

    public Card card() {
        if (card == null) {
            if (readOnly()) {
                return new Card().readOnly(true);
            }
            card = new Card();
            card.__setParent(this);
        }
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
        this.card.__setParent(this);
    }

    public Card getCard() {
        return this.card;
    }

}
