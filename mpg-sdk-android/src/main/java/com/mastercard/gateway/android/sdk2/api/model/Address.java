package com.mastercard.gateway.android.sdk2.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Address implements Parcelable {

    @Nullable
    public abstract String city();

    @Nullable
    public abstract String company();

    @Nullable
    public abstract String country();

    @Nullable
    public abstract String postcodeZip();

    @Nullable
    public abstract String stateProvince();

    @Nullable
    public abstract String street();

    @Nullable
    public abstract String street2();


    public static TypeAdapter<Address> typeAdapter(Gson gson) {
        return new AutoValue_Address.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Address.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder city(String city);

        public abstract Builder company(String company);

        public abstract Builder country(String country);

        public abstract Builder postcodeZip(String postcodeZip);

        public abstract Builder stateProvince(String stateProvince);

        public abstract Builder street(String street);

        public abstract Builder street2(String street2);

        public abstract Address build();
    }
}
