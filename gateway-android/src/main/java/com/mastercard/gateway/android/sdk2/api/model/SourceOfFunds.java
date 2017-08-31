package com.mastercard.gateway.android.sdk2.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class SourceOfFunds implements Parcelable {

    @Nullable
    public abstract Provided provided();


    public static TypeAdapter<SourceOfFunds> typeAdapter(Gson gson) {
        return new AutoValue_SourceOfFunds.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_SourceOfFunds.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder provided(Provided provided);

        public abstract SourceOfFunds build();
    }
}
