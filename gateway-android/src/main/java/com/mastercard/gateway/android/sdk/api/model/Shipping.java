package com.mastercard.gateway.android.sdk.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Shipping implements Parcelable {

    @Nullable
    public abstract String method();

    @Nullable
    public abstract Address address();

    @Nullable
    public abstract Contact contact();


    public static TypeAdapter<Shipping> typeAdapter(Gson gson) {
        return new AutoValue_Shipping.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Shipping.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder method(String method);

        public abstract Builder address(Address address);

        public abstract Builder contact(Contact contact);

        public abstract Shipping build();
    }
}
