-keep class * extends androidx.viewbinding.ViewBinding { *; }

-keepattributes Signature
# 保留 ViewBinding 及其 inflate 方法
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static * inflate(android.view.LayoutInflater);
    public static * bind(android.view.View);
}