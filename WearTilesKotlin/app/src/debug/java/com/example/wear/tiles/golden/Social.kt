/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.FONT_WEIGHT_MEDIUM
import androidx.wear.protolayout.LayoutElementBuilders.FontSetting
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.TEXT_ALIGN_CENTER
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.expression.ProtoLayoutExperimental
import androidx.wear.protolayout.layout.basicText
import androidx.wear.protolayout.layout.fontStyle
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.TITLE_SMALL
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.clip
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.modifiers.toProtoLayoutModifiers
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.image
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources

fun Context.mockContacts(): List<Contact> {
  return listOf(
    Contact(
      initials = "MS",
      avatarId = resources.getResourceName(R.drawable.avatar_illustration_18),
      avatarResource = R.drawable.avatar_illustration_18
    ),
    Contact(initials = "AB", avatarId = null, avatarResource = null),
    Contact(
      initials = "WW",
      avatarId = resources.getResourceName(R.drawable.photo_17),
      avatarResource = R.drawable.photo_17
    ),
    Contact(initials = "CD", avatarId = null, avatarResource = null),
    Contact(
      initials = "AD",
      avatarId = resources.getResourceName(R.drawable.avatar_3d_24),
      avatarResource = R.drawable.avatar_3d_24
    ),
    Contact(initials = "EF", avatarId = null, avatarResource = null)
  )
}

data class Contact(
  val initials: String,
  val clickable: Clickable = clickable(),
  val avatarId: String?,
  @DrawableRes val avatarResource: Int?
)

@OptIn(ProtoLayoutExperimental::class)
fun MaterialScope.contactButton(contact: Contact): LayoutElement {
  if (contact.avatarId != null) {
    return image {
      setHeight(expand())
      setWidth(expand())
      setModifiers(LayoutModifier.clip(shapes.full).toProtoLayoutModifiers())
      setResourceId(contact.avatarId)
      setContentScaleMode(CONTENT_SCALE_MODE_CROP)
    }
  } else {
    val colors =
      listOf(
        ButtonColors(labelColor = colorScheme.onPrimary, containerColor = colorScheme.primaryDim),
        ButtonColors(
          labelColor = colorScheme.onSecondary,
          containerColor = colorScheme.secondaryDim
        ),
        ButtonColors(labelColor = colorScheme.onTertiary, containerColor = colorScheme.tertiaryDim)
      )[contact.initials.hashCode() % 3]
    return textButton(
      onClick = clickable(),
      labelContent = {
        basicText(
          text = contact.initials.layoutString,
          fontStyle =
          fontStyle(
            color = colors.labelColor,
            settings = listOf(FontSetting.width(60F)),
            size = 30F,
            weight = FONT_WEIGHT_MEDIUM
          ),
          modifier =
          LayoutModifier.background(colors.containerColor)
            .clip(shapes.full)
            .padding(horizontal = 2F, vertical = 2F)
        )
      },
      width = expand(),
      height = expand(),
      contentPadding = padding(horizontal = 4F, vertical = 2F),
      colors = colors
    )
  }
}

object Social {
  data class SocialData(val contacts: List<Contact>)

  fun layout(
    context: Context,
    deviceParameters: DeviceParameters,
    data: SocialData
  ): LayoutElement {
    return materialScope(
      context = context,
      deviceConfiguration = deviceParameters,
      allowDynamicTheme = true
    ) {
      val (row1, row2) =
        data.contacts.take(6).take(if (isLargeScreen()) 6 else 4).run {
          when (count()) {
            1 -> Pair(subList(0, 1), emptyList()) // 1 | 0 split
            2 -> Pair(subList(0, 2), emptyList()) // 2 | 0 split
            3 -> Pair(subList(0, 2), subList(2, 3)) // 2 | 1 split
            4 -> Pair(subList(0, 2), subList(2, 4)) // 2 | 2 split
            5 -> Pair(subList(0, 3), subList(3, 5)) // 3 | 2 split
            6 -> Pair(subList(0, 3), subList(3, 6)) // 3 | 3 split
            else ->
              throw IllegalArgumentException(
                "Unsupported contact count: ${count()}. Expected 1 to 6."
              )
          }
        }

      primaryLayout(
        titleSlot =
        if (row2.isEmpty()) {
          {
            text(
              text = "Contacts".layoutString,
              color = colorScheme.onBackground,
              typography = TITLE_SMALL,
              maxLines = 2,
              alignment = TEXT_ALIGN_CENTER,
              modifier = LayoutModifier.contentDescription("Contacts")
            )
          }
        } else {
          null
        },
        mainSlot = {
          column {
            setWidth(expand())
            setHeight(expand())
            addContent(buttonGroup { row1.forEach { buttonGroupItem { contactButton(it) } } })
            if (!row2.isEmpty()) {
              addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
              addContent(buttonGroup { row2.forEach { buttonGroupItem { contactButton(it) } } })
            }
          }
        },
        bottomSlot = {
          textEdgeButton(
            onClick = clickable(),
            labelContent = { text("More".layoutString) },
            colors =
            ButtonColors(
              labelColor = colorScheme.onSurface,
              containerColor = colorScheme.surfaceContainer
            )
          )
        }
      )
    }
  }

  fun resources(context: Context, contacts: List<Contact>) = resources {
    contacts.forEach {
      if (it.avatarId != null && it.avatarResource != null) {
        addIdToImageMapping(it.avatarId, it.avatarResource)
      }
    }
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview1(context: Context) = socialPreviewN(context, 1)

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview2(context: Context) = socialPreviewN(context, 2)

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview3(context: Context) = socialPreviewN(context, 3)

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview4(context: Context) = socialPreviewN(context, 4)

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview5(context: Context) = socialPreviewN(context, 5)

@MultiRoundDevicesWithFontScalePreviews
internal fun socialPreview6(context: Context) = socialPreviewN(context, 6)

internal fun socialPreviewN(context: Context, n: Int): TilePreviewData {
  val contacts = context.mockContacts().take(n)
  return TilePreviewData(Social.resources(context, contacts)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Social.layout(context, it.deviceConfiguration, Social.SocialData(contacts))
    )
      .build()
  }
}

class SocialTileService5 : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement {
    val contacts = context.mockContacts().take(5)
    return Social.layout(context, deviceParameters, Social.SocialData(contacts))
  }

  override fun resources(context: Context) =
    Social.resources(context, context.mockContacts().take(5))
}

class SocialTileService6 : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement {
    val contacts = context.mockContacts().take(6)
    return Social.layout(context, deviceParameters, Social.SocialData(contacts))
  }

  override fun resources(context: Context) =
    Social.resources(context, context.mockContacts().take(6))
}

class SocialTileService2 : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement {
    val contacts = context.mockContacts().take(2)
    return Social.layout(context, deviceParameters, Social.SocialData(contacts))
  }

  override fun resources(context: Context) =
    Social.resources(context, context.mockContacts().take(2))
}
