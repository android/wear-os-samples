package com.example.wear.tiles

import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.TimelineBuilders

/**
 * Provides a [TimelineBuilders.Timeline] which has only one entry.
 */
internal fun singleEntryTimeline(rootLayout: LayoutElement): TimelineBuilders.Timeline {
    return TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(rootLayout)
                        .build()
                )
                .build()
        )
        .build()
}
