package com.mastercard.gateway.android.sdk2.api;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.mastercard.gateway.android.sdk2.GatewayResponse;

@AutoValue
public abstract class UpdateSessionResponse implements GatewayResponse, Parcelable {

    @Nullable
    public abstract String correlationId();

    @Nullable
    public abstract String session();

    @Nullable
    public abstract String version();


    public static TypeAdapter<UpdateSessionResponse> typeAdapter(Gson gson) {
        return new AutoValue_UpdateSessionResponse.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_UpdateSessionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder correlationId(String correlationId);

        public abstract Builder session(String session);

        public abstract Builder version(String version);

        public abstract UpdateSessionResponse build();
    }
}
