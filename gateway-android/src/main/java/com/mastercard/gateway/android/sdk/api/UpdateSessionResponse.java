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

package com.mastercard.gateway.android.sdk.api;


import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class UpdateSessionResponse implements GatewayResponse, Parcelable {

    @Nullable
    public abstract String correlationId();

    @Nullable
    @SerializedName("session")
    public abstract String sessionId();

    @Nullable
    public abstract String version();


    public static TypeAdapter<UpdateSessionResponse> typeAdapter(Gson gson) {
        return new AutoValue_UpdateSessionResponse.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
        return new AutoValue_UpdateSessionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder correlationId(String correlationId);

        public abstract Builder sessionId(String sessionId);

        public abstract Builder version(String version);

        public abstract UpdateSessionResponse build();
    }
}
