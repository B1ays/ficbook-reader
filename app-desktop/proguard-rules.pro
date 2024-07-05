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
-dontwarn **
-dontpreverify

-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
-keepclassmembers class androidx.compose.material3.DrawerValue { *; }
-keepclassmembers class androidx.activity.compose.ComponentActivityKt { *; }
#-keep class androidx.compose.material3.** { *; }

# Decompose
-keep class com.arkivanov.decompose.extensions.compose.jetbrains.mainthread.SwingMainThreadChecker
-keep class com.arkivanov.decompose.extensions.compose.mainthread.SwingMainThreadChecker

######

-dontusemixedcaseclassnames
-verbose

# Preserve some attributes that may be required for reflection.
#-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontnote android.support.**
-dontwarn android.support.**

-dontwarn javax.annotation.**

# Understand the @Keep support annotation.
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

### Realm ###
## Keep Companion classes and class.Companion member of all classes that can be used in our API to
#  allow calling realmObjectCompanionOrThrow and realmObjectCompanionOrNull on the classes
-keep class io.realm.kotlin.types.RealmInstant$Companion
-keepclassmembers class io.realm.kotlin.types.RealmInstant {
    io.realm.kotlin.types.RealmInstant$Companion Companion;
}
-keep class org.mongodb.kbson.BsonObjectId$Companion
-keepclassmembers class org.mongodb.kbson.BsonObjectId {
    org.mongodb.kbson.BsonObjectId$Companion Companion;
}
-keep class io.realm.kotlin.dynamic.DynamicRealmObject$Companion, io.realm.kotlin.dynamic.DynamicMutableRealmObject$Companion
-keepclassmembers class io.realm.kotlin.dynamic.DynamicRealmObject, io.realm.kotlin.dynamic.DynamicMutableRealmObject {
    **$Companion Companion;
}
-keep,allowobfuscation class ** implements io.realm.kotlin.types.BaseRealmObject
-keep class ** implements io.realm.kotlin.internal.RealmObjectCompanion
-keepclassmembers class ** implements io.realm.kotlin.types.BaseRealmObject {
    **$Companion Companion;
}

## Preserve all native method names and the names of their classes.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

## Preserve all classes that are looked up from native code
# Notification callback
-keep class io.realm.kotlin.internal.interop.NotificationCallback {
    *;
}
# Utils to convert core errors into Kotlin exceptions
-keep class io.realm.kotlin.internal.interop.CoreErrorConverter {
    *;
}
-keep class io.realm.kotlin.internal.interop.JVMScheduler {
    *;
}
# Interop, sync-specific classes
-keep class io.realm.kotlin.internal.interop.sync.NetworkTransport {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.Response {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.LongPointerWrapper {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.AppError {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.CoreConnectionState {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.SyncError {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.LogCallback {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.SyncErrorCallback {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.JVMSyncSessionTransferCompletionCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.ResponseCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.ResponseCallbackImpl {
    *;
}
-keep class io.realm.kotlin.internal.interop.AppCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.CompactOnLaunchCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.MigrationCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.DataInitializationCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.SubscriptionSetCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.SyncBeforeClientResetHandler {
    *;
}
-keep class io.realm.kotlin.internal.interop.SyncAfterClientResetHandler {
    *;
}
-keep class io.realm.kotlin.internal.interop.AsyncOpenCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.NativePointer {
    *;
}
-keep class io.realm.kotlin.internal.interop.ProgressCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.ApiKeyWrapper {
    *;
}
-keep class io.realm.kotlin.internal.interop.ConnectionStateChangeCallback {
    *;
}
-keep class io.realm.kotlin.internal.interop.SyncThreadObserver {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.CoreCompensatingWriteInfo {
    *;
}
# Preserve Function<X> methods as they back various functional interfaces called from JNI
-keep class kotlin.jvm.functions.Function* {
    *;
}
-keep class kotlin.Unit {
    *;
}

# Platform networking callback
-keep class io.realm.kotlin.internal.interop.sync.WebSocketTransport {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.CancellableTimer {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.WebSocketClient {
    *;
}
-keep class io.realm.kotlin.internal.interop.sync.WebSocketObserver {
    *;
}
-keep class io.realm.kotlin.Deleteable {
    *;
}
-keep class io.realm.kotlin.types.RealmObject {
    *;
}
-keep class io.realm.kotlin.jvm.SoLoader {
    *;
}

# Un-comment for debugging
#-printconfiguration /tmp/full-r8-config.txt
#-keepattributes LineNumberTable,SourceFile
#-printusage /tmp/removed_entries.txt
#-printseeds /tmp/kept_entries.txt