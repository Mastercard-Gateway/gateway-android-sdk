
package com.mastercard.gateway.android.sdkold.merchant.session;

import com.mastercard.gateway.android.sdkold.Payload;
import com.mastercard.gateway.android.sdkold.ValidationException;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.Billing;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.Customer;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.Device;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.Session;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.Shipping;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.SourceOfFunds;

public class UpdateSessionWithPayerDataRequest
    extends Payload
{

    private final static long serialVersionUID = 1;
    private String apiOperation;
    private String correlationId;
    private Billing billing;
    private Customer customer;
    private Device device;
    private Session session;
    private Shipping shipping;
    private SourceOfFunds sourceOfFunds;

    @Override
    public void validate()
        throws ValidationException
    {
        if (apiOperation == null) {
            throw new ValidationException((__thisPath()+"apiOperation"));
        }
        if (billing!= null) {
            billing.validate();
        }
        if (customer!= null) {
            customer.validate();
        }
        if (device!= null) {
            device.validate();
        }
        if (session!= null) {
            session.validate();
        }
        if (shipping!= null) {
            shipping.validate();
        }
        if (sourceOfFunds!= null) {
            sourceOfFunds.validate();
        }
    }

    @Override
    public UpdateSessionWithPayerDataRequest readOnly(boolean b) {
        super.readOnly(b);
        if (billing!= null) {
            billing.readOnly(b);
        }
        if (customer!= null) {
            customer.readOnly(b);
        }
        if (device!= null) {
            device.readOnly(b);
        }
        if (session!= null) {
            session.readOnly(b);
        }
        if (shipping!= null) {
            shipping.readOnly(b);
        }
        if (sourceOfFunds!= null) {
            sourceOfFunds.readOnly(b);
        }
        return this;
    }

    public String apiOperation() {
        return apiOperation;
    }

    public UpdateSessionWithPayerDataRequest apiOperation(String apiOperation) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.apiOperation = apiOperation;
        }
        return this;
    }

    public String getApiOperation() {
        return this.apiOperation;
    }

    public void setApiOperation(String apiOperation) {
        this.apiOperation = apiOperation;
    }

    public String correlationId() {
        return correlationId;
    }

    public UpdateSessionWithPayerDataRequest correlationId(String correlationId) {
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

    public Billing billing() {
        if (billing == null) {
            if (readOnly()) {
                return new Billing().readOnly(true);
            }
            billing = new Billing();
            billing.__setParent(this);
        }
        return billing;
    }

    public void setBilling(Billing billing) {
        this.billing = billing;
        this.billing.__setParent(this);
    }

    public Billing getBilling() {
        return this.billing;
    }

    public Customer customer() {
        if (customer == null) {
            if (readOnly()) {
                return new Customer().readOnly(true);
            }
            customer = new Customer();
            customer.__setParent(this);
        }
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        this.customer.__setParent(this);
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public Device device() {
        if (device == null) {
            if (readOnly()) {
                return new Device().readOnly(true);
            }
            device = new Device();
            device.__setParent(this);
        }
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
        this.device.__setParent(this);
    }

    public Device getDevice() {
        return this.device;
    }

    public Session session() {
        if (session == null) {
            if (readOnly()) {
                return new Session().readOnly(true);
            }
            session = new Session();
            session.__setParent(this);
        }
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
        this.session.__setParent(this);
    }

    public Session getSession() {
        return this.session;
    }

    public Shipping shipping() {
        if (shipping == null) {
            if (readOnly()) {
                return new Shipping().readOnly(true);
            }
            shipping = new Shipping();
            shipping.__setParent(this);
        }
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
        this.shipping.__setParent(this);
    }

    public Shipping getShipping() {
        return this.shipping;
    }

    public SourceOfFunds sourceOfFunds() {
        if (sourceOfFunds == null) {
            if (readOnly()) {
                return new SourceOfFunds().readOnly(true);
            }
            sourceOfFunds = new SourceOfFunds();
            sourceOfFunds.__setParent(this);
        }
        return sourceOfFunds;
    }

    public void setSourceOfFunds(SourceOfFunds sourceOfFunds) {
        this.sourceOfFunds = sourceOfFunds;
        this.sourceOfFunds.__setParent(this);
    }

    public SourceOfFunds getSourceOfFunds() {
        return this.sourceOfFunds;
    }

}
