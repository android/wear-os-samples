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
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
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
 * Responsive Small Hero widget template implementing Figma redline pattern (node `2:2165`).
 *
 * Consists of a full-width pill button container with an app icon badge and a headline title.
 * Automatically adapts padding and badge sizing between narrow circular and wide squircle displays.
 *
 * @param title Primary title text.
 * @param iconRes Drawable resource ID for the badge icon.
 * @param onClick Action triggered on button click.
 * @param colors Styling color tokens.
 * @param config Responsive layout configuration.
 * @param modifier Root layout modifier.
 */
@RemoteComposable
@Composable
fun SmallHeroWidgetTemplate(
    title: String,
    @DrawableRes iconRes: Int = R.drawable.ic_bolt_24,
    onClick: Action = valueChange(rememberMutableRemoteInt(0), 0.ri),
    colors: WidgetTemplateColors = WidgetTemplateColors(),
    config: WidgetTemplateConfig = LocalWidgetTemplateConfig.current,
    modifier: RemoteModifier = RemoteModifier.fillMaxSize(),
) {
    RemoteRow(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(onClick)
                .padding(horizontal = if (config.isNarrow) 12.rdp else 16.rdp),
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        RemoteBox(
            modifier =
                RemoteModifier.size(config.badgeSize)
                    .clip(RemoteCircleShape)
                    .background(colors.badgeBackgroundColor),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteIcon(
                imageVector = ImageVector.vectorResource(id = iconRes),
                contentDescription = title.rs,
                modifier = RemoteModifier.size(config.badgeIconSize),
                tint = colors.badgeIconTint,
            )
        }

        RemoteBox(modifier = RemoteModifier.size(config.contentGap))

        RemoteText(
            text = title.rs,
            color = colors.contentColor,
            fontSize = if (config.isNarrow) 14.rsp else 16.rsp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Standalone widget wrapping [SmallHeroWidgetTemplate]. */
class SmallHeroWidget(
    private val title: String = "Title",
    @param:DrawableRes private val iconRes: Int = R.drawable.ic_bolt_24,
    private val colors: WidgetTemplateColors = WidgetTemplateColors(),
) : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val config = WidgetTemplateConfig.from(params)
        return WearWidgetDocument(background = WearWidgetBrush.color(colors.containerColor)) {
            SmallHeroWidgetTemplate(
                title = title,
                iconRes = iconRes,
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
fun SmallHeroWidgetSquirclePreview(
    @PreviewParameter(SquircleAllWidgetPreviewParams::class) params: WearWidgetParams
) = WearWidgetPreview(SmallHeroWidget(), params)

@Preview(name = "Round Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun SmallHeroWidgetRoundPreview(
    @PreviewParameter(RoundAllWidgetPreviewParams::class) params: WearWidgetParams
) = WearWidgetPreview(SmallHeroWidget(), params)

@Preview(name = "Widget Picker Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun SmallHeroWidgetRectangularPreview(
    @PreviewParameter(RectangularAllWidgetPreviewParams::class) params: WearWidgetParams
) = WearWidgetPreview(SmallHeroWidget(), params)
