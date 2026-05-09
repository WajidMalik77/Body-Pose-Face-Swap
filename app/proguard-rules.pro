# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# ============================================================
# RETROFIT
# ============================================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ============================================================
# GSON (used with Retrofit converter)
# ============================================================
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

# ============================================================
# YOUR APP MODELS (Retrofit/Gson response classes)
# ============================================================
-keep class com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.models.** { *; }
-keep class com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.** { *; }

# ============================================================
# OKHTTP / LOGGING INTERCEPTOR
# ============================================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================================
# FIREBASE
# ============================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Remote Config
-keep class com.google.firebase.remoteconfig.** { *; }

# Firebase Realtime Database
-keep class com.google.firebase.database.** { *; }
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName *;
}

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ============================================================
# GOOGLE ADMOB / PLAY SERVICES ADS
# ============================================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ============================================================
# GOOGLE BILLING
# ============================================================
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ============================================================
# GLIDE
# ============================================================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# ============================================================
# UCROP
# ============================================================
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# ============================================================
# GEMINI AI (Google Generative AI)
# ============================================================
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ============================================================
# COROUTINES
# ============================================================
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ============================================================
# NAVIGATION COMPONENT (Safe Args)
# ============================================================
-keep class * extends androidx.navigation.NavArgs { *; }
-keep class * extends androidx.navigation.NavArgsLazy { *; }
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# ============================================================
# STICKER (local module)
# ============================================================
-keep class com.xiaopo.flying.sticker.** { *; }
-dontwarn com.xiaopo.flying.sticker.**

# ============================================================
# ROOZI ADS (local module)
# ============================================================
-keep class roozi.app.ads.** { *; }
-keep class roozi.app.** { *; }
-dontwarn roozi.app.**

# ============================================================
# COLOR PICKER
# ============================================================
-keep class com.skydoves.colorpickerview.** { *; }
-dontwarn com.skydoves.colorpickerview.**

# ============================================================
# LOCALE HELPER
# ============================================================
-keep class com.zeugmasolutions.localehelper.** { *; }
-dontwarn com.zeugmasolutions.localehelper.**

# ============================================================
# VIEWBINDING & GENERAL ANDROID
# ============================================================
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(...);
    public static * bind(android.view.View);
}

# ============================================================
# KOTLIN
# ============================================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy {
    <fields>;
}

# ============================================================
# ENUMS
# ============================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================
# PARCELABLE
# ============================================================
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ============================================================
# SERIALIZABLE
# ============================================================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================================
# KEEP LINE NUMBERS FOR CRASHLYTICS
# ============================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# SUPPRESS COMMON WARNINGS
# ============================================================
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Facebook Infer Annotations (used internally by Facebook SDK / Audience Network)
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe