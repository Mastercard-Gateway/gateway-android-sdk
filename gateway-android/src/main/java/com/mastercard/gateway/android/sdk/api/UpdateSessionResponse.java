package com.mastercard.gateway.android.sdk.api;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class UpdateSessionResponse implements GatewayResponse, Parcelable {

    @Nullable
    public abstract String correlationId();

    @Nullable
    @SerializedName("session")
    public abstract String sessionId();

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

        public abstract Builder sessionId(String sessionId);

        public abstract Builder version(String version);

        public abstract UpdateSessionResponse build();
    }
}
