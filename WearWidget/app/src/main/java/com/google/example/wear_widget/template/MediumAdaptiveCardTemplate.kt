/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:SuppressLint("RestrictedApi")

package com.google.example.wear_widget.template

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.remote.creation.compose.action.Action
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxHeight
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.RectangularAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteText
import com.google.example.wear_widget.R

/**
 * Responsive Medium 2-Row Adaptive Card widget template implementing Figma redline pattern (node
 * `2:2703`).
 *
 * Consists of:
 * - Left card surface filling available width:
 *     - Row 1: Header lockup with leading circular icon badge and section title.
 *     - Row 2: Headline metric text with optional subtitle.
 * - Right trailing column with up to two stacked circular action buttons.
 *
 * Automatically reflows between narrow round screens and wide squircle screens.
 *
 * @param title Header title text.
 * @param headline Primary headline metric (e.g. "12,450" or "This is a\nHeadline").
 * @param subtitle Optional secondary metadata line.
 * @param headerIconRes Drawable resource ID for the header badge icon.
 * @param onCardClick Action triggered when tapping the primary card.
 * @param topActionIconRes Optional drawable resource for top trailing action button.
 * @param onTopActionClick Action triggered by top action button.
 * @param bottomActionIconRes Optional drawable resource for bottom trailing action button.
 * @param onBottomActionClick Action triggered by bottom action button.
 * @param colors Styling color tokens.
 * @param config Responsive layout configuration.
 * @param modifier Root layout modifier.
 */
@RemoteComposable
@Composable
fun MediumAdaptiveCardTemplate(
    title: String,
    headline: String,
    subtitle: String? = null,
    @DrawableRes headerIconRes: Int = R.drawable.ic_bolt_24,
    onCardClick: Action = valueChange(rememberMutableRemoteInt(0), 0.ri),
    @DrawableRes topActionIconRes: Int? = R.drawable.ic_sparkle_24,
    onTopActionClick: Action = valueChange(rememberMutableRemoteInt(0), 0.ri),
    @DrawableRes bottomActionIconRes: Int? = R.drawable.ic_sparkle_24,
    onBottomActionClick: Action = valueChange(rememberMutableRemoteInt(0), 0.ri),
    colors: WidgetTemplateColors = WidgetTemplateColors(),
    config: WidgetTemplateConfig = LocalWidgetTemplateConfig.current,
    modifier: RemoteModifier = RemoteModifier.fillMaxSize(),
) {
    val hasActions = topActionIconRes != null || bottomActionIconRes != null

    RemoteRow(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        // Left main card (fills remaining width)
        RemoteColumn(
            modifier =
                RemoteModifier.weight(1f)
                    .fillMaxHeight()
                    .clip(RemoteRoundedCornerShape(config.cardRadius))
                    .background(colors.containerColor)
                    .clickable(onCardClick)
                    .padding(
                        horizontal = if (config.isNarrow) 10.rdp else 14.rdp,
                        vertical = if (config.isNarrow) 8.rdp else 12.rdp,
                    ),
            verticalArrangement = RemoteArrangement.SpaceBetween,
        ) {
            // Header Row: Badge + Title
            RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically) {
                RemoteBox(
                    modifier =
                        RemoteModifier.size(config.badgeSize)
                            .clip(RemoteCircleShape)
                            .background(colors.badgeBackgroundColor),
                    contentAlignment = RemoteAlignment.Center,
                ) {
                    RemoteIcon(
                        imageVector = ImageVector.vectorResource(id = headerIconRes),
                        contentDescription = title.rs,
                        modifier = RemoteModifier.size(config.badgeIconSize),
                        tint = colors.badgeIconTint,
                    )
                }

                RemoteBox(modifier = RemoteModifier.size(config.contentGap))

                RemoteText(
                    text = title.rs,
                    color = colors.contentColor,
                    fontSize = if (config.isNarrow) 12.rsp else 14.rsp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Headline + Optional Subtitle
            RemoteColumn {
                headline.split("\n").forEach { line ->
                    RemoteText(
                        text = line.rs,
                        color = colors.contentColor,
                        fontSize = if (config.isNarrow) 18.rsp else 22.rsp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (subtitle != null) {
                    RemoteBox(modifier = RemoteModifier.size(2.rdp))
                    RemoteText(
                        text = subtitle.rs,
                        color = colors.secondaryContentColor,
                        fontSize = if (config.isNarrow) 10.rsp else 12.rsp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Trailing column: 2 stacked circular action buttons
        if (hasActions) {
            RemoteBox(modifier = RemoteModifier.size(config.contentGap))

            RemoteColumn(
                modifier = RemoteModifier.fillMaxHeight(),
                verticalArrangement = RemoteArrangement.SpaceBetween,
                horizontalAlignment = RemoteAlignment.CenterHorizontally,
            ) {
                if (topActionIconRes != null) {
                    RemoteBox(
                        modifier =
                            RemoteModifier.size(config.actionButtonSize)
                                .clip(RemoteCircleShape)
                                .background(colors.actionButtonColor)
                                .clickable(onTopActionClick),
                        contentAlignment = RemoteAlignment.Center,
                    ) {
                        RemoteIcon(
                            imageVector = ImageVector.vectorResource(id = topActionIconRes),
                            contentDescription = "Top Action".rs,
                            modifier = RemoteModifier.size(20.rdp),
                            tint = colors.actionIconTint,
                        )
                    }
                }

                if (bottomActionIconRes != null) {
                    RemoteBox(
                        modifier =
                            RemoteModifier.size(config.actionButtonSize)
                                .clip(RemoteCircleShape)
                                .background(colors.actionButtonColor)
                                .clickable(onBottomActionClick),
                        contentAlignment = RemoteAlignment.Center,
                    ) {
                        RemoteIcon(
                            imageVector = ImageVector.vectorResource(id = bottomActionIconRes),
                            contentDescription = "Bottom Action".rs,
                            modifier = RemoteModifier.size(20.rdp),
                            tint = colors.actionIconTint,
                        )
                    }
                }
            }
        }
    }
}

/** Standalone widget wrapping [MediumAdaptiveCardTemplate]. */
class MediumAdaptiveCardWidget(
    private val title: String = "Title",
    private val headline: String = "Headline",
    private val subtitle: String? = null,
    @param:DrawableRes private val headerIconRes: Int = R.drawable.ic_bolt_24,
    @param:DrawableRes private val topActionIconRes: Int? = R.drawable.ic_sparkle_24,
    @param:DrawableRes private val bottomActionIconRes: Int? = R.drawable.ic_sparkle_24,
    private val colors: WidgetTemplateColors = WidgetTemplateColors(),
) : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val config = WidgetTemplateConfig.from(params)
        return WearWidgetDocument(background = WearWidgetBrush.color(Color.Transparent.rc)) {
            MediumAdaptiveCardTemplate(
                title = title,
                headline = headline,
                subtitle = subtitle,
                headerIconRes = headerIconRes,
                topActionIconRes = topActionIconRes,
                bottomActionIconRes = bottomActionIconRes,
                colors = colors,
                config = config,
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------------------

@Preview(name = "Squircle Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun MediumAdaptiveCardSquirclePreview(
    @PreviewParameter(SquircleAllWidgetPreviewParams::class) params: WearWidgetParams
) =
    WearWidgetPreview(
        MediumAdaptiveCardWidget(title = "Title", headline = "This is a\nHeadline"),
        params,
    )

@Preview(name = "Round Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun MediumAdaptiveCardRoundPreview(
    @PreviewParameter(RoundAllWidgetPreviewParams::class) params: WearWidgetParams
) =
    WearWidgetPreview(
        MediumAdaptiveCardWidget(title = "Title", headline = "This is a\nHeadline"),
        params,
    )

@Preview(name = "Widget Picker Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun MediumAdaptiveCardRectangularPreview(
    @PreviewParameter(RectangularAllWidgetPreviewParams::class) params: WearWidgetParams
) =
    WearWidgetPreview(
        MediumAdaptiveCardWidget(title = "Title", headline = "This is a\nHeadline"),
        params,
    )
