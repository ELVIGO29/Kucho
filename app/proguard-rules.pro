# =====================================================
# Kucho - Proguard Rules
# =====================================================

# Retrofit + OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson - Mantener modelos de datos serializados
-keep class pe.kucho.app.data.model.** { *; }
-keepclassmembers class pe.kucho.app.data.model.** {
    <init>(...);
    <fields>;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# DataStore
-keep class androidx.datastore.** { *; }

# Security Crypto
-keep class androidx.security.crypto.** { *; }
