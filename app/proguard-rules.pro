# Keep Moshi adapters
-keep class com.serhio.money.data.network.dto.** { *; }
-keepclassmembers class com.serhio.money.data.network.dto.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room entities
-keep class com.serhio.money.data.database.entities.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.serhio.money.data.network.ExchangeRateApi

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Timber
-keep class timber.log.Timber { *; }

# Wear OS
-keep class androidx.wear.** { *; }
