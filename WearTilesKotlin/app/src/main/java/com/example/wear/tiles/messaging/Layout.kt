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
package com.example.wear.tiles.messaging

import android.content.Context
import androidx.annotation.OptIn
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.FontSetting
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.expression.ProtoLayoutExperimental
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.clip
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.modifiers.toProtoLayoutModifiers
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.image
import com.example.wear.tiles.tools.isLargeScreen

@OptIn(ProtoLayoutExperimental::class)
fun MaterialScope.contactButton(contact: Contact): LayoutElement {
  if (contact.avatarSource !is AvatarSource.None) {
    return image {
      setHeight(expand())
      setWidth(expand())
      setModifiers(LayoutModifier.clip(shapes.full).toProtoLayoutModifiers())
      setResourceId(contact.imageResourceId())
      setContentScaleMode(CONTENT_SCALE_MODE_CROP)
    }
  } else {
    // Simple function to return one of a set of themed button colors
    val colors = buttonColorsByIndex(contact.initials.hashCode())

    return textButton(
      onClick = clickable(),
      labelContent = {
        text(
          text = contact.initials.layoutString,
          color = colors.labelColor,
          settings = listOf(FontSetting.width(60F), FontSetting.weight(500))
        )
      },
      width = expand(),
      height = expand(),
      contentPadding = padding(horizontal = 4F, vertical = 2F),
      colors = colors
    )
  }
}

fun tileLayout(
  context: Context,
  deviceParameters: DeviceParameters,
  contacts: List<Contact>
): LayoutElement {
  return materialScope(
    context = context,
    deviceConfiguration = deviceParameters,
    allowDynamicTheme = true
  ) {
    val visibleContacts = contacts.take(if (isLargeScreen()) 6 else 4)

    val (row1, row2) =
      visibleContacts.chunked(if (visibleContacts.size > 4) 3 else 2).let { chunkedList ->
        Pair(
          chunkedList.getOrElse(0) { emptyList() },
          chunkedList.getOrElse(1) { emptyList() }
        )
      }

    primaryLayout(
      titleSlot =
      // Only display the title if there's one row, otherwise the touch targets become
      // too small (less than 48dp). See
      // https://developer.android.com/training/wearables/accessibility#set-minimum
      if (row2.isEmpty()) {
        { text(text = "Contacts".layoutString) }
      } else {
        null
      },
      mainSlot = {
        column {
          setWidth(expand())
          setHeight(expand())
          addContent(
            buttonGroup { row1.forEach { buttonGroupItem { contactButton(it) } } }
          )
          if (!row2.isEmpty()) {
            addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
            addContent(
              buttonGroup { row2.forEach { buttonGroupItem { contactButton(it) } } }
            )
          }
        }
      },
      bottomSlot = {
        textEdgeButton(
          onClick = clickable(),
          labelContent = { text("More".layoutString) },
          colors = filledTonalButtonColors()
        )
      }
    )
  }
}

/** Returns a set of [ButtonColors] based on the provided index [n]. */
private fun MaterialScope.buttonColorsByIndex(n: Int): ButtonColors =
  listOf(
    ButtonColors(
      labelColor = colorScheme.onPrimary,
      containerColor = colorScheme.primaryDim
    ),
    ButtonColors(
      labelColor = colorScheme.onSecondary,
      containerColor = colorScheme.secondaryDim
    ),
    ButtonColors(
      labelColor = colorScheme.onTertiary,
      containerColor = colorScheme.tertiaryDim
    )
  )
    .let { it[n.mod(it.size)] }

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
  val contacts = getMockLocalContacts().take(n)
  return TilePreviewData(
    resources {
      contacts.forEach {
        if (it.avatarSource is AvatarSource.Resource) {
          addIdToImageMapping(it.imageResourceId(), it.avatarSource.resourceId)
        }
      }
    }
  ) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      tileLayout(context, it.deviceConfiguration, contacts)
    )
      .build()
  }
}

internal fun resources(
  fn: ResourceBuilders.Resources.Builder.() -> Unit
): (RequestBuilders.ResourcesRequest) -> ResourceBuilders.Resources = {
  ResourceBuilders.Resources.Builder().setVersion(it.version).apply(fn).build()
}
