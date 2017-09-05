package com.mastercard.gateway.android.sdk.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Device implements Parcelable {

    @Nullable
    public abstract String browser();

    @Nullable
    public abstract String fingerprint();

    @Nullable
    public abstract String hostname();

    @Nullable
    public abstract String ipAddress();

    @Nullable
    public abstract String mobilePhoneModel();


    public static TypeAdapter<Device> typeAdapter(Gson gson) {
        return new AutoValue_Device.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Device.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder browser(String browser);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder hostname(String hostname);

        public abstract Builder ipAddress(String ipAddress);

        public abstract Builder mobilePhoneModel(String mobilePhoneModel);

        public abstract Device build();
    }
}
