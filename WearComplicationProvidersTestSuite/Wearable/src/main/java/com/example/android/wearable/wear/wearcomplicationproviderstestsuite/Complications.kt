package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

/**
 * The enumerated list of all complications provided by this app.
 */
enum class Complication(

    /**
     * A stable key that can be used to distinguish between this app's complications.
     */
    val key: String
) {
    ICON("Icon"),
    LARGE_IMAGE("LargeImage"),
    LONG_TEXT("LongText"),
    RANGED_VALUE("RangedValue"),
    SHORT_TEXT("ShortText"),
    SMALL_IMAGE("SmallImage")
}
