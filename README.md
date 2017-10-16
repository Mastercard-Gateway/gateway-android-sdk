# Gateway Android SDK Sample App

Our Android SDK allows you to easily integrate payments into your Android app. By updating a checkout session directly with the Gateway, you avoid the risk of handling sensitive card details on your server. This sample app demonstrates the basics of installing and configuring the SDK to complete a simple transaction.

## Initialize the Sample App

This sample app requires a running instance of our **[Gateway Test Merchant Server](https://github.com/Mastercard/gateway-test-merchant-server)**. Follow the instructions for that project and copy the resulting URL of the instance you create.

After you pull this sample app code down, open the *gradle.properties* file. There are three fields which must be completed in order for the sample app to run a test transaction.

```properties
# TEST Gateway Merchant ID
gatewayMerchantId=

# Gateway Base URL
gatewayBaseUrl=

# TEST Merchant Server URL (test server app deployed to Heroku)
# For more information, see: https://github.com/Mastercard/gateway-test-merchant-server
# ex: https://{your-app-name}.herokuapp.com
merchantServerUrl=
```

Once completed, you may run the sample app on your device.

## Integrating with Your App

### Import the Dependency [![Download](https://api.bintray.com/packages/mpgs/Android/gateway-android-sdk/images/download.svg)](https://bintray.com/mpgs/Android/gateway-android-sdk/_latestVersion)

This library is hosted in the jCenter repository. To import the Android SDK, include it as a dependency in your build.gradle file. Be sure to replace `{X.X.X}` with the version number in the shield above.

```groovy
compile 'com.mastercard.gateway:gateway-android:{X.X.X}'
```

### Configuring the SDK

In order to use the SDK, you must initialize the Gateway object with your merchant ID and the base URL of the API.

```java
String merchantId = "YOUR_MERCHANT_ID";
String baseUrl    = "https://na-gateway.mastercard.com";

Gateway gateway = new Gateway();
gateway.setMerchantId(merchantId);
gateway.setBaseUrl(baseUrl);
```

The SDK strictly enforces certificate pinning for added security. If you have a custom base URL (ie. **NOT** a *mastercard.com* domain), you will also need to provide the PEM-encoded SSL public certificate for that domain. We recommend using the intermediate certificate since it typically has a longer life-span than server certificates.

```java
String alias      = "mycustomcert";
String customCert = "MIIFAzCCA+ugAwIBAgIEUdNg7jANBgkq...";

gateway.addTrustedCertificate(alias, customCert);
```

### Updating a Session with Card Information

To help alleviate the worry of passing card information through your servers, the SDK provides a method to update a session with card data directly with the Gateway. Using an existing session ID, you can do so in a couple different ways:

```java
GatewayCallback<UpdateSessionResponse> callback = new GatewayCallback<UpdateSessionResponse>() {
    @Override
    public void onSuccess(UpdateSessionResponse response) {
        // TODO handle success
    }
    
    @Override
    public void onError(Throwable throwable) {
        // TODO handle error
    }
};

gateway.updateSessionWithCardInfo(sessionId, nameOnCard, cardNumber, securityCode, expiryMM, expiryYY, callback);
```

You can also construct a `Card` object and pass that as argument to the SDK.

```java
Card card = Card.builder()
    .nameOnCard("Joe Cardholder")
    .number("5111111111111118")
    .securityCode("100")
    .expiry(Expiry.builder()
        .month("05")
        .year("21")
        .build())
    .build();

gateway.updateSessionWithCardInfo(sessionId, card, callback);
```

Once card details have been sent, you can complete the Gateway session on your servers with the private API password.


### Rx-Enabled

If being reactive is your thing, then we've got you covered. Include the **[RxJava2](https://github.com/ReactiveX/RxJava)** library in your project and utilize the appropriate methods provided in the `Gateway` class.

```java
Single<UpdateSessionResponse> single = gateway.updateSessionWithCardInfo(session, card);
```

---

For more information, visit [https://na-gateway.mastercard.com/api/documentation/integrationGuidelines/index.html](https://test-gateway.mastercard.com/api/documentation/integrationGuidelines/index.html)
