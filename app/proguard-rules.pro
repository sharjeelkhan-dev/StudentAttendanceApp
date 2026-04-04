# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Room entities
-keep class com.attendance.app.data.local.entity.** { *; }

# Keep Gson serialization
-keep class com.attendance.app.data.backup.BackupData { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Hilt
-keep class dagger.hilt.** { *; }
