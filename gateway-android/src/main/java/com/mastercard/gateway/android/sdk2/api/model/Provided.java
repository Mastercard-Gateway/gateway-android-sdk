package com.mastercard.gateway.android.sdk2.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Provided implements Parcelable {

    @Nullable
    public abstract Card card();


    public static TypeAdapter<Provided> typeAdapter(Gson gson) {
        return new AutoValue_Provided.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Provided.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder card(Card card);

        public abstract Provided build();
    }
}
