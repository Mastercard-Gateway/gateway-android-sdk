# Gateway Android SDK Sample App [![Build Status](https://travis-ci.org/Mastercard/gateway-android-sdk.svg?branch=master)](https://travis-ci.org/Mastercard/gateway-android-sdk)

Our Android SDK allows you to easily integrate payments into your Android app. By updating a checkout session directly with the Gateway, you avoid the risk of handling sensitive card details on your server. This sample app demonstrates the basics of installing and configuring the SDK to complete a simple transaction.

## Basic Transaction Flow Diagram

![Transaction Flow](./transaction-flow.png "Transaction Flow")

## Initialize the Sample App

This sample app requires a running instance of our **[Gateway Test Merchant Server]**. Follow the instructions for that project and copy the resulting URL of the instance you create.

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

---

# Integrating with Your App

## Import the Dependency [![Download](https://api.bintray.com/packages/mpgs/Android/gateway-android-sdk/images/download.svg)](https://bintray.com/mpgs/Android/gateway-android-sdk/_latestVersion) 

This library is hosted in the jCenter repository. To import the Android SDK, include it as a dependency in your build.gradle file. Be sure to replace `{X.X.X}` with the version number in the shield above.

```groovy
implementation 'com.mastercard.gateway:gateway-android:{X.X.X}'
```

## Configuring the SDK

In order to use the SDK, you must initialize the Gateway object with your merchant ID and the base URL of the API.

```java
String merchantId = "YOUR_MERCHANT_ID";
String baseUrl    = "YOUR_GATEWAY_BASE_URL"; // ex: "https://na-gateway.mastercard.com"

Gateway gateway = new Gateway();
gateway.setMerchantId(merchantId);
gateway.setBaseUrl(baseUrl);
```

The SDK strictly enforces [certificate pinning] for added security. If you have a custom base URL (ie. **NOT** a *mastercard.com* domain), you will also need to provide the PEM-encoded SSL public certificate for your domain. We recommend using the intermediate certificate since it typically has a longer life-span than server certificates. Read more about how to obtain this certificate in the **Certificate Pinning** section.

```java
String alias      = "mycustomcert";
String customCert = "MIIFAzCCA+ugAwIBAgIEUdNg7jANBgkq...";

gateway.addTrustedCertificate(alias, customCert);
```

## Updating a Session with Card Information

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


## Rx-Enabled

If being reactive is your thing, then we've got you covered. Include the **[RxJava2]** library in your project and utilize the appropriate methods provided in the `Gateway` class.

```java
Single<UpdateSessionResponse> single = gateway.updateSessionWithCardInfo(session, card);
```


## Certificate Pinning

[Certificate pinning] is a security measure used to prevent man-in-the-middle attacks by reducing the number of trusted certificate authorities from the default list to only those you provide. If your gateway instance is not a *mastercard.com* URL, then you will need to provide a valid X.509 certificate for that domain. We recommend using the 'intermediate' certificate, as it typically has a much longer life-span than the 'leaf' certificate issued for your domain.

One easy method of retrieving this certificate is to download it through your browser.
1. In the **Chrome** browser, navigate to your gateway integration guide. (ie. https://<your-gateway-domain>/api/documentation)
1. Right-click on the page and click *Inspect* in the menu
1. Select the *Security* tab in the inspector and click *View certificate*
1. In the popup window, click on the intermediate certificate (most likely the middle certificate in the chain)
1. In the info window below, drag the large certificate icon onto your desktop, downloading the *.cer* file to your machine
1. Rename this file to something simple, like *gateway.cer*, and copy it into your project's *assets* or *raw* directory

With the certificate now stored in your app, you can add it as a parameter to the Gateway SDK as an InputStream.

If you prefer to store the certificate as a String constant rather than a resource file, you can convert the certificate to PEM format using the following command:
```
openssl x509 -inform der -in <downloaded-certificate.cer> -out gateway.pem
```
The entire content of this generated PEM file (including header, footer, and new line characters) should be provided as a parameter to the Gateway SDK.



[Gateway Test Merchant Server]: https://github.com/Mastercard/gateway-test-merchant-server
[certificate pinning]: https://en.wikipedia.org/wiki/HTTP_Public_Key_Pinning
[RxJava2]: https://github.com/ReactiveX/RxJava
[integration guidelines]: https://na-gateway.mastercard.com/api/documentation/integrationGuidelines/index.html
