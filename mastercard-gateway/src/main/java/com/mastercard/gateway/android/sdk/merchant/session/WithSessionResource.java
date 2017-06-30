
package com.mastercard.gateway.android.sdk.merchant.session;

import com.mastercard.gateway.android.sdk.AbstractResource;
import com.mastercard.gateway.android.sdk.Comms;
import com.mastercard.gateway.android.sdk.CommsException;
import com.mastercard.gateway.android.sdk.GatewayErrorException;

public class WithSessionResource
    extends AbstractResource
{

    private final String merchantId;
    private final String sessionId;

    public WithSessionResource(Comms comms, String merchantId, String sessionId) {
        super(comms, "https://eu-gateway.mastercard.com/api/rest/version/39");
        this.merchantId = merchantId;
        this.sessionId = sessionId;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public UpdateSessionWithPayerDataResponse updateSessionWithPayerData(UpdateSessionWithPayerDataRequest request)
        throws CommsException, GatewayErrorException
    {
        return send("PUT", ("/merchant/"+this.merchantId+"/session/"+this.sessionId+""), request, UpdateSessionWithPayerDataResponse.class);
    }

}
