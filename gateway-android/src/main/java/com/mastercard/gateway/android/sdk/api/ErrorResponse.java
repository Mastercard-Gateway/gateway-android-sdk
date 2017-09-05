package com.mastercard.gateway.android.sdk.api;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.mastercard.gateway.android.sdk.api.model.Error;

@AutoValue
public abstract class ErrorResponse implements Parcelable {

    @Nullable
    public abstract Result result();

    @Nullable
    public abstract Error error();


    public static TypeAdapter<ErrorResponse> typeAdapter(Gson gson) {
        return new AutoValue_ErrorResponse.GsonTypeAdapter(gson);
    }



    public static Builder builder() {
        return new AutoValue_ErrorResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder result(Result result);

        public abstract Builder error(Error error);

        public abstract ErrorResponse build();
    }


    /**
     * A system-generated high level overall result of the operation.
     */
    public enum Result {

        /**
         * The operation resulted in an error and hence cannot be processed.
         */
        ERROR
    }
}
