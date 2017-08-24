package com.mastercard.gateway.android.sdk2.api;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.mastercard.gateway.android.sdk2.GatewayCallback;
import com.mastercard.gateway.android.sdk2.api.model.Billing;
import com.mastercard.gateway.android.sdk2.api.model.Customer;
import com.mastercard.gateway.android.sdk2.api.model.Device;
import com.mastercard.gateway.android.sdk2.api.model.Session;
import com.mastercard.gateway.android.sdk2.api.model.Shipping;
import com.mastercard.gateway.android.sdk2.api.model.SourceOfFunds;

@AutoValue
public abstract class UpdateSessionRequest implements Parcelable {

    public interface Callback extends GatewayCallback<UpdateSessionResponse> {}

    @Nullable
    public abstract String apiOperation();

    @Nullable
    public abstract String correlationId();

    @Nullable
    public abstract Shipping shipping();

    @Nullable
    public abstract Billing billing();

    @Nullable
    public abstract Customer customer();

    @Nullable
    public abstract Device device();

    @Nullable
    public abstract Session session();

    @Nullable
    public abstract SourceOfFunds sourceOfFunds();


    public static TypeAdapter<UpdateSessionRequest> typeAdapter(Gson gson) {
        return new AutoValue_UpdateSessionRequest.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_UpdateSessionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder apiOperation(String apiOperation);

        public abstract Builder correlationId(String correlationId);

        public abstract Builder shipping(Shipping shipping);

        public abstract Builder billing(Billing billing);

        public abstract Builder customer(Customer customer);

        public abstract Builder device(Device device);

        public abstract Builder session(Session session);

        public abstract Builder sourceOfFunds(SourceOfFunds sourceOfFunds);

        public abstract UpdateSessionRequest build();
    }
}
