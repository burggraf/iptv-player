# Koin
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.scope.Scope

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Media3
-keep class androidx.media3.** { *; }
