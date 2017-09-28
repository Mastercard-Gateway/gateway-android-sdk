## proguard
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

## GSON ##
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# keep api contract and enums
-keep class com.mastercard.gateway.android.sdk.api.** { *; }
-keep enum com.mastercard.gateway.android.sdk.** { *; }

# Optional libraries will warn on missing classes
-dontwarn io.reactivex.**