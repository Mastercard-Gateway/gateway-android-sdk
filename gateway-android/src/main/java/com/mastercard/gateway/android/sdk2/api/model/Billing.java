package com.mastercard.gateway.android.sdk2.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Billing implements Parcelable {

    @Nullable
    public abstract Address address();


    public static TypeAdapter<Billing> typeAdapter(Gson gson) {
        return new AutoValue_Billing.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Billing.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(Address address);

        public abstract Billing build();
    }
}
