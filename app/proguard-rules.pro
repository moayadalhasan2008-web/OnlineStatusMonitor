# ProGuard rules for release builds.

# Keep Room entities/DAOs
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

