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

import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class HttpRequest {

    public enum Method {GET, PUT, POST, DELETE, HEAD, TRACE}


    @Nullable
    public abstract String endpoint();

    @Nullable
    public abstract Method method();

    @Nullable
    public abstract String payload();

    @Nullable
    public abstract String contentType();

    @Nullable
    public abstract List<Pair<String, Object>> headers();


    public abstract HttpRequest withEndpoint(String endpoint);

    public abstract HttpRequest withMethod(Method method);

    public abstract HttpRequest withPayload(String payload);

    public abstract HttpRequest withContentType(String contentType);

    public abstract HttpRequest withHeaders(List<Pair<String, Object>> headers);



    public static Builder builder() {
        return new AutoValue_HttpRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder endpoint(String endpoint);

        public abstract Builder method(Method method);

        public abstract Builder payload(String payload);

        public abstract Builder contentType(String contentType);

        public abstract Builder headers(List<Pair<String, Object>> headers);

        public abstract HttpRequest build();
    }
}
