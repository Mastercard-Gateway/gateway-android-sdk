package com.mastercard.gateway.android.sdk.api;


public enum Region {
    TEST("test"),
    EUROPE("eu"),
    NORTH_AMERICA("na"),
    ASIA_PACIFIC("ap");

    String urlPrefix;

    Region(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getUrlPrefix() {
        return this.urlPrefix;
    }
}
