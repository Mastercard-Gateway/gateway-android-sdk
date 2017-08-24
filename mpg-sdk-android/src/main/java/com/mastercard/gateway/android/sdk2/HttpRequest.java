package com.mastercard.gateway.android.sdk2;

import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
abstract class HttpRequest {

    public enum Method {GET, PUT, POST, DELETE, HEAD, TRACE}

    public abstract String endpoint();

    public abstract Method method();

    @Nullable
    public abstract String payload();

    @Nullable
    public abstract String contentType();

    @Nullable
    public abstract List<Pair<String, Object>> headers();


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
