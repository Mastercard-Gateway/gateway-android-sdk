/*
 * Copyright (c) 2016 Mastercard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mastercard.gateway.android.sdk.api.model;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class Error implements Parcelable {

    @Nullable
    public abstract Cause cause();

    @Nullable
    public abstract String explanation();

    @Nullable
    public abstract String field();

    @Nullable
    public abstract String supportCode();

    @Nullable
    public abstract ValidationType validationType();


    public static TypeAdapter<Error> typeAdapter(Gson gson) {
        return new AutoValue_Error.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_Error.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder cause(Cause cause);

        public abstract Builder explanation(String explanation);

        public abstract Builder field(String field);

        public abstract Builder supportCode(String supportCode);

        public abstract Builder validationType(ValidationType validationType);

        public abstract Error build();
    }

    /**
     * Broadly categorizes the cause of the error.
     * For example, errors may occur due to invalid requests or internal system failures.
     */
    public enum Cause {
        /**
         * The request was rejected due to security reasons such as firewall rules, expired certificate, etc.
         */
        REQUEST_REJECTED,

        /**
         * The request was rejected because it did not conform to the API protocol.
         */
        INVALID_REQUEST,

        /**
         * There was an internal system failure.
         */
        SERVER_FAILED,

        /**
         * The server did not have enough resources to process the request at the moment.
         */
        SERVER_BUSY
    }

    /**
     * Indicates the type of field validation error.
     * This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public enum ValidationType {
        /**
         * The request contained a field with a value that did not pass validation.
         */
        INVALID,

        /**
         * The request was missing a mandatory field.
         */
        MISSING,

        /**
         * The request contained a field that is unsupported.
         */
        UNSUPPORTED
    }
}
