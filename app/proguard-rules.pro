# Keep rules para Retrofit / kotlinx.serialization / Coil (se refinan con el primer build release).
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Retrofit: keep the service interfaces (and their generic signatures) so the
# kotlinx.serialization converter can resolve the suspend return types. Without
# this, R8 strips the Continuation<T> signature and Retrofit asks for a converter
# for java.lang.Object (seen on stats() as "iu2.b").
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Retrofit 3 + R8 full mode: preserve the coroutine continuation and Call types
# (and their generics) that carry the response type of suspend methods.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# kotlinx.serialization: mantener serializers.
-keep,includedescriptorclasses class app.ocphotos.**$$serializer { *; }
-keepclassmembers class app.ocphotos.** {
    *** Companion;
}
-keepclasseswithmembers class app.ocphotos.** {
    kotlinx.serialization.KSerializer serializer(...);
}
