/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.watchfacekotlin.config

import com.example.android.wearable.watchfacekotlin.config.ColorSelectionActivity.Companion.EXTRA_SHARED_PREF
import java.io.InvalidClassException

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast

import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView

import com.example.android.wearable.watchfacekotlin.R
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData.BackgroundComplicationConfigItem
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData.ColorConfigItem
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData.ConfigItemType
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData.MoreOptionsConfigItem
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData.PreviewAndComplicationsConfigItem
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData.UnreadNotificationConfigItem
import com.example.android.wearable.watchfacekotlin.watchface.AnalogComplicationWatchFaceService

import java.util.concurrent.Executors

/**
 * Displays different layouts for configuring watch face's complications and appearance settings
 * (highlight color [second arm], background color, unread notifications, etc.).
 *
 *
 * All appearance settings are saved via [SharedPreferences].
 *
 *
 * Layouts provided by this adapter are split into 5 main view types.
 *
 *
 * A watch face preview including complications. Allows user to tap on the complications to
 * change the complication data and see a live preview of the watch face.
 *
 *
 * Simple arrow to indicate there are more options below the fold.
 *
 *
 * Color configuration options for both highlight (seconds hand) and background color.
 *
 *
 * Toggle for unread notifications.
 *
 *
 * Background image complication configuration for changing background image of watch face.
 */
class AnalogComplicationConfigRecyclerViewAdapter(
    private val context: Context,
    watchFaceServiceClass: Class<*>,
    private val settingsDataSet: ArrayList<ConfigItemType>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * Used by associated watch face ([AnalogComplicationWatchFaceService]) to let this
     * adapter know which complication locations are supported, their ids, and supported
     * complication data types.
     */
    enum class ComplicationLocation {
        BACKGROUND, LEFT, RIGHT, TOP, BOTTOM
    }

    // ComponentName associated with watch face service (service that renders watch face). Used
    // to retrieve complication information.
    private val watchFaceComponentName: ComponentName = ComponentName(context, watchFaceServiceClass)

    var sharedPref: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.analog_complication_preference_file_key),
        Context.MODE_PRIVATE
    )

    // Selected complication id by user (default value is invalid [only changed when user taps to
    // change complication]).
    private var selectedComplicationId: Int = -1
    private val backgroundComplicationId: Int =
        AnalogComplicationWatchFaceService.getComplicationId(ComplicationLocation.BACKGROUND)

    private val leftComplicationId: Int =
        AnalogComplicationWatchFaceService.getComplicationId(ComplicationLocation.LEFT)

    private val rightComplicationId: Int =
        AnalogComplicationWatchFaceService.getComplicationId(ComplicationLocation.RIGHT)

    // Required to retrieve complication data from watch face for preview.
    private val providerInfoRetriever: ProviderInfoRetriever =
        ProviderInfoRetriever(context, Executors.newCachedThreadPool())

    // Maintains reference view holder to dynamically update watch face preview. Used instead of
    // notifyItemChanged(int position) to avoid flicker and re-inflating the view.
    private lateinit var previewAndComplicationsViewHolder: PreviewAndComplicationsViewHolder

    init {
        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever.init()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder(): viewType: $viewType")

        val viewHolder: RecyclerView.ViewHolder

        when (viewType) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                // Need direct reference to watch face preview view holder to update watch face
                // preview based on selections from the user.
                previewAndComplicationsViewHolder = PreviewAndComplicationsViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.config_list_preview_and_complications_item,
                        parent,
                        false
                    )
                )
                viewHolder = previewAndComplicationsViewHolder
            }

            TYPE_MORE_OPTIONS -> viewHolder = MoreOptionsViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_more_options_item,
                    parent,
                    false
                )
            )

            TYPE_COLOR_CONFIG -> viewHolder = ColorPickerViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_color_item,
                    parent,
                    false)
            )

            TYPE_UNREAD_NOTIFICATION_CONFIG -> viewHolder = UnreadNotificationViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_unread_notif_item,
                    parent,
                    false
                )
            )

            TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG -> viewHolder =
                BackgroundComplicationViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(
                            R.layout.config_list_background_complication_item,
                            parent,
                            false
                        )
                )

            else -> throw InvalidClassException("Config type must be valid type.")
        }
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "Element $position set.")

        // Pulls all data required for creating the UX for the specific setting option.
        val configItemType: ConfigItemType = settingsDataSet[position]

        when (viewHolder.itemViewType) {

            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder: PreviewAndComplicationsViewHolder =
                    viewHolder as PreviewAndComplicationsViewHolder

                // Retrieve config item (contains data to populate view).
                val previewAndComplicationsConfigItem: PreviewAndComplicationsConfigItem =
                    configItemType as PreviewAndComplicationsConfigItem

                // Retrieve specific icon needed for view holder.
                val defaultComplicationResourceId: Int =
                    previewAndComplicationsConfigItem.defaultComplicationResourceId

                // Set icon in view holder and initialize colors/complications.
                previewAndComplicationsViewHolder.setDefaultComplicationDrawable(
                    defaultComplicationResourceId
                )

                previewAndComplicationsViewHolder.initializesColorsAndComplications()
            }

            TYPE_MORE_OPTIONS -> {
                val moreOptionsViewHolder: MoreOptionsViewHolder =
                    viewHolder as MoreOptionsViewHolder

                // Retrieve config item (contains data to populate view).
                val moreOptionsConfigItem: MoreOptionsConfigItem =
                    configItemType as MoreOptionsConfigItem

                // Get and set icon in view holder.
                moreOptionsViewHolder.setIcon(moreOptionsConfigItem.iconResourceId)
            }

            TYPE_COLOR_CONFIG -> {
                val colorPickerViewHolder: ColorPickerViewHolder =
                    viewHolder as ColorPickerViewHolder

                // Retrieve config item (contains data to populate view).
                val colorConfigItem: ColorConfigItem = configItemType as ColorConfigItem

                // Retrieve specific icons, strings, etc. needed for view holder.
                val iconResourceId: Int = colorConfigItem.iconResourceId
                val name: String = colorConfigItem.name
                val sharedPrefString: String = colorConfigItem.sharedPrefString
                val activity: Class<ColorSelectionActivity> =
                    colorConfigItem.getActivityToChoosePreference()

                // Set icons, strings, etc. in view holder.
                colorPickerViewHolder.setIcon(iconResourceId)
                colorPickerViewHolder.setName(name)
                colorPickerViewHolder.setSharedPrefString(sharedPrefString)
                colorPickerViewHolder.setLaunchActivityToSelectColor(activity)
            }

            TYPE_UNREAD_NOTIFICATION_CONFIG -> {

                val unreadViewHolder: UnreadNotificationViewHolder =
                    viewHolder as UnreadNotificationViewHolder

                // Retrieve config item (contains data to populate view).
                val unreadConfigItem: UnreadNotificationConfigItem =
                    configItemType as UnreadNotificationConfigItem

                // Retrieve specific icons, strings, etc. needed for view holder.
                val unreadEnabledIconResourceId: Int = unreadConfigItem.iconEnabledResourceId
                val unreadDisabledIconResourceId: Int = unreadConfigItem.iconDisabledResourceId
                val unreadName: String = unreadConfigItem.name
                val unreadSharedPrefId: Int = unreadConfigItem.sharedPrefId

                // Set icons, strings, etc. in view holder.
                unreadViewHolder.setIcons(
                    unreadEnabledIconResourceId, unreadDisabledIconResourceId
                )
                unreadViewHolder.setName(unreadName)
                unreadViewHolder.setSharedPrefId(unreadSharedPrefId)
            }

            TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG -> {
                val backgroundComplicationViewHolder: BackgroundComplicationViewHolder =
                    viewHolder as BackgroundComplicationViewHolder

                // Retrieve config item (contains data to populate view).
                val backgroundComplicationConfigItem: BackgroundComplicationConfigItem =
                    configItemType as BackgroundComplicationConfigItem

                // Retrieve specific icons, strings, etc. needed for view holder.
                val backgroundIconResourceId: Int = backgroundComplicationConfigItem.iconResourceId
                val backgroundName: String = backgroundComplicationConfigItem.name

                // Set icons, strings, etc. in view holder.
                backgroundComplicationViewHolder.setIcon(backgroundIconResourceId)
                backgroundComplicationViewHolder.setName(backgroundName)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val configItemType: ConfigItemType = settingsDataSet[position]
        return configItemType.configType
    }

    override fun getItemCount() = settingsDataSet.size

    /** Updates the selected complication id saved earlier with the new information.  */
    fun updateSelectedComplication(complicationProviderInfo: ComplicationProviderInfo?) {
        Log.d(TAG, "updateSelectedComplication: $previewAndComplicationsViewHolder")

        // Checks if view is inflated and complication id is valid.
        if (selectedComplicationId >= 0) {
            previewAndComplicationsViewHolder.updateComplicationViews(
                selectedComplicationId,
                complicationProviderInfo
            )
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        // Required to release retriever for active complication data on detach.
        providerInfoRetriever.release()
    }

    fun updatePreviewColors() {
        Log.d(TAG, "updatePreviewColors(): $previewAndComplicationsViewHolder")
        previewAndComplicationsViewHolder.updateWatchFaceColors()
    }

    /**
     * Displays watch face preview along with complication locations. Allows user to tap on the
     * complication they want to change and preview updates dynamically.
     */
    inner class PreviewAndComplicationsViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

        private val watchFaceArmsAndTicksView: View
        private val watchFaceHighlightPreviewView: View
        private val watchFaceBackgroundPreviewImageView: ImageView

        private val leftComplicationBackground: ImageView
        private val rightComplicationBackground: ImageView
        private val leftComplication: ImageButton
        private val rightComplication: ImageButton
        private var defaultComplicationDrawable: Drawable? = null
        private var backgroundComplicationEnabled = false

        override fun onClick(view: View) {
            if (view == leftComplication) {
                Log.d(TAG, "Left Complication click()")
                val currentActivity = view.context as Activity
                launchComplicationHelperActivity(currentActivity, ComplicationLocation.LEFT)

            } else if (view == rightComplication) {
                Log.d(TAG, "Right Complication click()")
                val currentActivity = view.context as Activity
                launchComplicationHelperActivity(currentActivity, ComplicationLocation.RIGHT)
            }
        }

        fun updateWatchFaceColors() {

            // Only update background colors for preview if background complications are disabled.
            if (!backgroundComplicationEnabled) {
                // Updates background color.
                val backgroundSharedPrefString = context.getString(R.string.saved_background_color)
                val currentBackgroundColor =
                    sharedPref.getInt(backgroundSharedPrefString, Color.BLACK)
                val backgroundColorFilter =
                    PorterDuffColorFilter(currentBackgroundColor, PorterDuff.Mode.SRC_ATOP)
                watchFaceBackgroundPreviewImageView
                    .background.colorFilter = backgroundColorFilter
            } else {
                // Inform user that they need to disable background image for color to work.
                val text: CharSequence = "Selected image overrides background color."
                val duration = Toast.LENGTH_LONG
                val toast = Toast.makeText(context, text, duration)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }

            // Updates highlight color (just second arm).
            val highlightSharedPrefString = context.getString(R.string.saved_marker_color)
            val currentHighlightColor = sharedPref.getInt(highlightSharedPrefString, Color.RED)
            val highlightColorFilter =
                PorterDuffColorFilter(currentHighlightColor, PorterDuff.Mode.SRC_ATOP)
            watchFaceHighlightPreviewView.background.colorFilter = highlightColorFilter
        }

        // Verifies the watch face supports the complication location, then launches the helper
        // class, so user can choose their complication data provider.
        private fun launchComplicationHelperActivity(
            currentActivity: Activity,
            complicationLocation: ComplicationLocation
        ) {
            selectedComplicationId =
                AnalogComplicationWatchFaceService.getComplicationId(complicationLocation)

            backgroundComplicationEnabled = false

            if (selectedComplicationId >= 0) {
                val supportedTypes: IntArray =
                    AnalogComplicationWatchFaceService.getSupportedComplicationTypes(
                        complicationLocation
                    )

                val watchFace = ComponentName(
                    currentActivity,
                    AnalogComplicationWatchFaceService::class.java
                )

                currentActivity.startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                        currentActivity,
                        watchFace,
                        selectedComplicationId,
                        *supportedTypes
                    ),
                    AnalogComplicationConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE
                )
            } else {
                Log.d(TAG, "Complication not supported by watch face.")
            }
        }

        fun setDefaultComplicationDrawable(resourceId: Int) {
            val context = watchFaceArmsAndTicksView.context

            defaultComplicationDrawable = context.getDrawable(resourceId)

            leftComplication.setImageDrawable(defaultComplicationDrawable)
            leftComplicationBackground.visibility = View.INVISIBLE

            rightComplication.setImageDrawable(defaultComplicationDrawable)
            rightComplicationBackground.visibility = View.INVISIBLE
        }

        fun updateComplicationViews(
            watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?
        ) {
            Log.d(TAG, "updateComplicationViews(): id: $watchFaceComplicationId")
            Log.d(TAG, "\tinfo: $complicationProviderInfo")
            if (watchFaceComplicationId == backgroundComplicationId) {
                if (complicationProviderInfo != null) {
                    backgroundComplicationEnabled = true

                    // Since we can't get the background complication image outside of the
                    // watch face, we set the icon for that provider instead with a gray background.
                    val backgroundColorFilter =
                        PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
                    watchFaceBackgroundPreviewImageView
                        .background.colorFilter = backgroundColorFilter
                    watchFaceBackgroundPreviewImageView.setImageIcon(
                        complicationProviderInfo.providerIcon
                    )
                } else {
                    backgroundComplicationEnabled = false

                    // Clears icon for background if it was present before.
                    watchFaceBackgroundPreviewImageView.setImageResource(
                        android.R.color.transparent
                    )
                    val backgroundSharedPrefString =
                        context.getString(R.string.saved_background_color)
                    val currentBackgroundColor =
                        sharedPref.getInt(backgroundSharedPrefString, Color.BLACK)
                    val backgroundColorFilter = PorterDuffColorFilter(
                        currentBackgroundColor, PorterDuff.Mode.SRC_ATOP
                    )
                    watchFaceBackgroundPreviewImageView
                        .background.colorFilter = backgroundColorFilter
                }
            } else if (watchFaceComplicationId == leftComplicationId) {
                updateComplicationView(
                    complicationProviderInfo, leftComplication,
                    leftComplicationBackground
                )
            } else if (watchFaceComplicationId == rightComplicationId) {
                updateComplicationView(
                    complicationProviderInfo, rightComplication,
                    rightComplicationBackground
                )
            }
        }

        private fun updateComplicationView(
            complicationProviderInfo: ComplicationProviderInfo?,
            button: ImageButton, background: ImageView
        ) {
            if (complicationProviderInfo != null) {
                button.setImageIcon(complicationProviderInfo.providerIcon)
                button.contentDescription = context.getString(
                    R.string.edit_complication,
                    complicationProviderInfo.appName + " " +
                            complicationProviderInfo.providerName
                )
                background.visibility = View.VISIBLE
            } else {
                button.setImageDrawable(defaultComplicationDrawable)
                button.contentDescription = context.getString(R.string.add_complication)
                background.visibility = View.INVISIBLE
            }
        }

        fun initializesColorsAndComplications() {

            // Initializes highlight color (just second arm and part of complications).
            val highlightSharedPrefString = context.getString(R.string.saved_marker_color)
            val currentHighlightColor = sharedPref.getInt(highlightSharedPrefString, Color.RED)
            val highlightColorFilter =
                PorterDuffColorFilter(currentHighlightColor, PorterDuff.Mode.SRC_ATOP)
            watchFaceHighlightPreviewView.background.colorFilter = highlightColorFilter

            // Initializes background color to gray (updates to color or complication icon based
            // on whether the background complication is live or not.
            val backgroundColorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
            watchFaceBackgroundPreviewImageView.background.colorFilter = backgroundColorFilter

            val complicationIds: IntArray = AnalogComplicationWatchFaceService.complicationIds
            providerInfoRetriever.retrieveProviderInfo(
                object : OnProviderInfoReceivedCallback() {
                    override fun onProviderInfoReceived(
                        watchFaceComplicationId: Int,
                        complicationProviderInfo: ComplicationProviderInfo?
                    ) {
                        Log.d(TAG, "onProviderInfoReceived: $complicationProviderInfo")
                        updateComplicationViews(watchFaceComplicationId, complicationProviderInfo)
                    }
                },
                watchFaceComponentName,
                *complicationIds
            )
        }

        init {
            watchFaceBackgroundPreviewImageView =
                view.findViewById<View>(R.id.watch_face_background) as ImageView
            watchFaceArmsAndTicksView = view.findViewById(R.id.watch_face_arms_and_ticks)

            // In our case, just the second arm.
            watchFaceHighlightPreviewView = view.findViewById(R.id.watch_face_highlight)

            // Sets up left complication preview.
            leftComplicationBackground =
                view.findViewById<View>(R.id.left_complication_background) as ImageView
            leftComplication = view.findViewById<View>(R.id.left_complication) as ImageButton
            leftComplication.setOnClickListener(this)

            // Sets up right complication preview.
            rightComplicationBackground =
                view.findViewById<View>(R.id.right_complication_background) as ImageView
            rightComplication = view.findViewById<View>(R.id.right_complication) as ImageButton
            rightComplication.setOnClickListener(this)
        }
    }

    /** Displays icon to indicate there are more options below the fold.  */
    inner class MoreOptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val moreOptionsImageView: ImageView
        fun setIcon(resourceId: Int) {
            val context = moreOptionsImageView.context
            moreOptionsImageView.setImageDrawable(context.getDrawable(resourceId))
        }

        init {
            moreOptionsImageView =
                view.findViewById<View>(R.id.more_options_image_view) as ImageView
        }
    }

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    inner class ColorPickerViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private val appearanceButton: Button
        private var sharedPrefResourceString: String? = null
        private var launchActivityToSelectColor: Class<ColorSelectionActivity>? = null
        fun setName(name: String?) {
            appearanceButton.text = name
        }

        fun setIcon(resourceId: Int) {
            val context = appearanceButton.context
            appearanceButton.setCompoundDrawablesWithIntrinsicBounds(
                context.getDrawable(resourceId), null, null, null
            )
        }

        fun setSharedPrefString(sharedPrefString: String?) {
            sharedPrefResourceString = sharedPrefString
        }

        fun setLaunchActivityToSelectColor(activity: Class<ColorSelectionActivity>?) {
            launchActivityToSelectColor = activity
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            Log.d(TAG, "Complication onClick() position: $position")
            if (launchActivityToSelectColor != null) {
                val launchIntent = Intent(view.context, launchActivityToSelectColor)

                // Pass shared preference name to save color value to.
                launchIntent.putExtra(EXTRA_SHARED_PREF, sharedPrefResourceString)
                val activity = view.context as Activity
                activity.startActivityForResult(
                    launchIntent,
                    AnalogComplicationConfigActivity.UPDATE_COLORS_CONFIG_REQUEST_CODE
                )
            }
        }

        init {
            appearanceButton = view.findViewById<View>(R.id.color_picker_button) as Button
            view.setOnClickListener(this)
        }
    }

    /**
     * Displays switch to indicate whether or not icon appears for unread notifications. User can
     * toggle on/off.
     */
    inner class UnreadNotificationViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private val unreadNotificationSwitch: Switch?
        private var enabledIconResourceId = 0
        private var disabledIconResourceId = 0
        private var sharedPrefResourceId = 0
        fun setName(name: String?) {
            unreadNotificationSwitch!!.text = name
        }

        fun setIcons(enabledIconResourceId: Int, disabledIconResourceId: Int) {
            this.enabledIconResourceId = enabledIconResourceId
            this.disabledIconResourceId = disabledIconResourceId
            val context = unreadNotificationSwitch!!.context

            // Set default to enabled.
            unreadNotificationSwitch.setCompoundDrawablesWithIntrinsicBounds(
                context.getDrawable(this.enabledIconResourceId), null, null, null
            )
        }

        fun setSharedPrefId(sharedPrefId: Int) {
            sharedPrefResourceId = sharedPrefId
            if (unreadNotificationSwitch != null) {
                val context = unreadNotificationSwitch.context
                val sharedPreferenceString = context.getString(sharedPrefResourceId)
                val currentState = sharedPref.getBoolean(sharedPreferenceString, true)
                updateIcon(context, currentState)
            }
        }

        private fun updateIcon(context: Context, currentState: Boolean) {
            val currentIconResourceId: Int =
                if (currentState) {
                    enabledIconResourceId
                } else {
                    disabledIconResourceId
                }
            unreadNotificationSwitch!!.isChecked = currentState
            unreadNotificationSwitch.setCompoundDrawablesWithIntrinsicBounds(
                context.getDrawable(currentIconResourceId), null, null, null
            )
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            Log.d(TAG, "Complication onClick() position: $position")
            val context = view.context
            val sharedPreferenceString = context.getString(sharedPrefResourceId)

            // Since user clicked on a switch, new state should be opposite of current state.
            val newState = !sharedPref.getBoolean(sharedPreferenceString, true)

            sharedPref.edit { putBoolean(sharedPreferenceString, newState) }

            updateIcon(context, newState)
        }

        init {
            unreadNotificationSwitch =
                view.findViewById<View>(R.id.unread_notification_switch) as Switch
            view.setOnClickListener(this)
        }
    }

    /** Displays button to trigger background image complication selector.  */
    inner class BackgroundComplicationViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private val backgroundComplicationButton: Button
        fun setName(name: String?) {
            backgroundComplicationButton.text = name
        }

        fun setIcon(resourceId: Int) {
            val context = backgroundComplicationButton.context
            backgroundComplicationButton.setCompoundDrawablesWithIntrinsicBounds(
                context.getDrawable(resourceId), null, null, null
            )
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            Log.d(TAG, "Background Complication onClick() position: $position")
            val currentActivity = view.context as Activity
            selectedComplicationId = AnalogComplicationWatchFaceService.getComplicationId(
                ComplicationLocation.BACKGROUND
            )
            if (selectedComplicationId >= 0) {
                val supportedTypes: IntArray =
                    AnalogComplicationWatchFaceService.getSupportedComplicationTypes(
                        ComplicationLocation.BACKGROUND
                    )
                val watchFace = ComponentName(
                    currentActivity, AnalogComplicationWatchFaceService::class.java
                )
                currentActivity.startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                        currentActivity,
                        watchFace,
                        selectedComplicationId,
                        *supportedTypes
                    ),
                    AnalogComplicationConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE
                )
            } else {
                Log.d(TAG, "Complication not supported by watch face.")
            }
        }

        init {
            backgroundComplicationButton =
                view.findViewById<View>(R.id.background_complication_button) as Button
            view.setOnClickListener(this)
        }
    }

    companion object {
        private const val TAG = "CompConfigAdapter"
        const val TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 0
        const val TYPE_MORE_OPTIONS = 1
        const val TYPE_COLOR_CONFIG = 2
        const val TYPE_UNREAD_NOTIFICATION_CONFIG = 3
        const val TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG = 4
    }
}
