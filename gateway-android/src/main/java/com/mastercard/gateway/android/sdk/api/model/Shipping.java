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
