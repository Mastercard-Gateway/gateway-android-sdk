package com.mastercard.gateway.android.sdk.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Card implements Parcelable {

    @Nullable
    public abstract String nameOnCard();

    @Nullable
    public abstract String number();

    @Nullable
    public abstract String securityCode();

    @Nullable
    public abstract Expiry expiry();


    public static TypeAdapter<Card> typeAdapter(Gson gson) {
        return new AutoValue_Card.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Card.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nameOnCard(String nameOnCard);

        public abstract Builder number(String number);

        public abstract Builder securityCode(String securityCode);

        public abstract Builder expiry(Expiry expiry);

        public abstract Card build();
    }
}
