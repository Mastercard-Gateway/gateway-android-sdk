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
