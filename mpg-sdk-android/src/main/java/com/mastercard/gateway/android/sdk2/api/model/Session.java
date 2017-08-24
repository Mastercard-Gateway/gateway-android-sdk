package com.mastercard.gateway.android.sdk2.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Session implements Parcelable {

    @Nullable
    public abstract String version();


    public static TypeAdapter<Session> typeAdapter(Gson gson) {
        return new AutoValue_Session.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Session.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Session build();
    }
}
