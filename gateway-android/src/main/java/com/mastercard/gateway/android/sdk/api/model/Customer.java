package com.mastercard.gateway.android.sdk.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Customer implements Parcelable {

    @Nullable
    public abstract String email();

    @Nullable
    public abstract String firstName();

    @Nullable
    public abstract String lastName();

    @Nullable
    public abstract String mobilePhone();

    @Nullable
    public abstract String phone();


    public static TypeAdapter<Customer> typeAdapter(Gson gson) {
        return new AutoValue_Customer.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Customer.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder email(String email);

        public abstract Builder firstName(String firstName);

        public abstract Builder lastName(String lastName);

        public abstract Builder mobilePhone(String mobilePhone);

        public abstract Builder phone(String phone);

        public abstract Customer build();
    }
}
