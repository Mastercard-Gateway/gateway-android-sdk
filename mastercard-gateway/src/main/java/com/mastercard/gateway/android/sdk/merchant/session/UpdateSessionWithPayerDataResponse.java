
package com.mastercard.gateway.android.sdk.merchant.session;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;

public class UpdateSessionWithPayerDataResponse
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String correlationId;
    private String session;
    private String version;

    @Override
    public void validate()
        throws ValidationException
    {
        if (session == null) {
            throw new ValidationException((__thisPath()+"session"));
        }
        if (version == null) {
            throw new ValidationException((__thisPath()+"version"));
        }
    }

    @Override
    public UpdateSessionWithPayerDataResponse readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    public String correlationId() {
        return correlationId;
    }

    public UpdateSessionWithPayerDataResponse correlationId(String correlationId) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.correlationId = correlationId;
        }
        return this;
    }

    public String getCorrelationId() {
        return this.correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String session() {
        return session;
    }

    public UpdateSessionWithPayerDataResponse session(String session) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.session = session;
        }
        return this;
    }

    public String getSession() {
        return this.session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String version() {
        return version;
    }

    public UpdateSessionWithPayerDataResponse version(String version) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.version = version;
        }
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
