# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/chiontang/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5
-dontusemixedcaseclassnames ##[混淆时不会产生形形色色的类名 】
-dontskipnonpubliclibraryclasses #[指定不去忽略非公共的库类。 】
-dontpreverify #[不预校验】
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/* #[优化】
-ignorewarnings

-keepattributes Signature
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep class **.R$* {
 *;
}
-keep class com.zyx.smarttouch.wxapi.WXEntryActivity{
*;
}

-keep class com.tencent.mm.sdk.** {
   *;
}

-keep class net.sqlcipher.** {
   *;
}

-keepclasseswithmembernames class * { #[保护指定的类和类的成员的名称，如果所有指定的类成员出席（在压缩步骤之后）】
native <methods>;
}

-keepclasseswithmembers class * { #[保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在。】
public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {#[保护指定的类文件和类的成员】
public static final android.os.Parcelable$Creator *;
}

-keep public class com.zyx.smarttouch.common.Aes {*; }

-keepclassmembers public class com.zyx.smarttouch.gui.RegeditActivity{
    public static boolean isRegedit();
    public static String getProduct();
    public static String getBrand();
}

-keep public class android.support.**{
    *;
}