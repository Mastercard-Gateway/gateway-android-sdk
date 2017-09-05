package com.mastercard.gateway.android.sdk.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Expiry implements Parcelable{

    @Nullable
    public abstract String month();

    @Nullable
    public abstract String year();


    public static TypeAdapter<Expiry> typeAdapter(Gson gson) {
        return new AutoValue_Expiry.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Expiry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder month(String month);

        public abstract Builder year(String year);

        public abstract Expiry build();
    }
}
