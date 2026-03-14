Wear OS Samples Repository
======================

This repository contains a set of individual Android Studio projects to help you get started writing Wear OS apps and watch faces.

Read below for a description of each sample.


Samples
----------

* **[ComposeStarter](ComposeStarter)** (Compose/Kotlin) - Demonstrates simple Compose for Wear OS app devs can use as a starting point for their own Compose app.

* **[DataLayer](DataLayer)** (Compose/Kotlin) - Demonstrates communicating via the data layer between the watch and the phone. [Guide](https://developer.android.com/training/wearables/data-layer)

* **[WatchFaceFormat](WatchFaceFormat)** - Demos the new Watch Face Format which allows quick development of performant watch
    faces in XML [Guide](https://developer.android.com/training/wearables/wff)

* **[Complications](Complications)** (Kotlin) - If you are writing a watch face with complications, this app gives you a full suite of data sources to test against your implementation of complications to make sure it looks good. [Complication Guide](https://developer.android.com/training/wearables/watch-faces/adding-complications)

* **[WearOAuth](WearOAuth)** (Kotlin) - Demonstrates how developers can authenticate a user on their Wear OS app via the user's mobile/phone device without requiring a mobile app (Wear OS companion app handles the request on the mobile side). The sample uses OAuth. [Guide](https://developer.android.com/training/wearables/apps/auth-wear)

* **[WearSpeakerSample](WearSpeakerSample)** (Compose/Kotlin) - Demonstrates audio recording and playback if the wearable device has a speaker. This is also demonstrate how to handling permissions. [Guide](https://developer.android.com/training/wearables/wearable-sounds)

* **[WearTilesKotlin](WearTilesKotlin)** (Kotlin) - Demonstrates tiles using the new AndroidX library. [Guide](https://developer.android.com/training/articles/wear-tiles)

* **[WatchFacePush](WatchFacePush)** (Kotlin) - Demonstrates the Watch Face Push AndroidX Library. [Guide](https://developer.android.com/training/wearables/watch-face-push)

* **Developing Complications**

In the process of developing complications, there are a few pitfalls that may be encountered and the information provided below should help the developer navigate them.

**Example complication sample layout:**

<Complication slot>
<BoundingOval>
<DefaultProviderPolicy>
<Complication>
<PartText>
<Text>
<Font>
<Template>
**Explanation of Complication parts:**

*   **_Complication slot_**
    *   Defines where on the watchface it is rendered, its shape, and what types of data it can take.
*   **_BoundingOval, BoundingBox, or BoundingArc_**
    *   Used to define a sub area of the complication slot. Tapping on it navigates the user to the relevant system setting or app.
*   **_DefaultProviderPolicy_**
    *   Defines what data source to use for the complication when one is not specifically set or if a set data source is no longer available (e.g. A user uninstalls a non-default or custom complication)
*   **_Complication_**
    *   Used to define the data type(s) intended for use from the data source.
*   **_PartText (partial list of supported children)_**
    *   Used to define the location and format of the complication slot where text is to be rendered.
        MOVE BELOW TO ANOTHER SECTION OF THE DOCUMENT OR GLOSSARY:

    *   _Text_
        *   Used to actually display the text, instead of just defining the text location.
    *   _Font_
        *   Used to set the font family, font size, and font color for the text. **_Font size includes both font height and font weight._**
    *   _Template_
        *   Used to format the information from the complication data to display on the watch face.

**Testing changes to a Complication tag or its children:**

The developers ran into problems testing the changes related to the complications that made them appear to not be functional. After making changes to a complication, it is recommended to clear the user data from the emulated device to make sure that the expected complication settings are actually taking effect.

*   **_A cold boot of the emulator is generally helpful (PLACEHOLDER TEXT, BREAK INTO PROCEDURAL STEPS)_**
n 
**Additional notes:**

*   As of March 7, 2026, the team is working only with built-in complications that use data directly from the watch. May implement complications using API calls (e.g. weather) in later releases, but not MVP.
*   The team is refining a simple display with battery percent, <TimeText>, and date with month abbreviated (e.g. 6 Mar). Once this is satisfactory, we will add another complication, tentatively StepCount.

**Excerpts from meeting notes:**

*   **_February 28, 2026_**
    *   Describing something as a complication indicates that info needs to be pulled from widgets. Complication type indicates which data sources it should pull from. This indicated via Default Provider Policy. Integer id, e.g. 13 for day of week. WearOS handles binding automatically in response to the <Complication> XML tag.
    *   Tim is seeing an area of the watch that seems to have a complication, but nothing’s showing up. Clicking on it produces the error message “there is no intent to launch”.
    *   Clicking on an area with a complication produces a highlighted circle (brief and faint).
    *   Xml requires <ComplicationSlot>, then <Complication>.
    *   Placing <PartText> inside <ComplicationSlot> but outside <Complication> is not valid placement, because it’s not a valid child.
    *   <Compare> statement which will display its child if a condition becomes true. This could be used to show icons in response to different battery states.
    *   Key point: **_The complication is called WATCH\_BATTERY, not BATTERY\_PERCENT._**
*   **_March 2, 2026_**
    *   Officially, <Complication Slot> ids go from 0 up to 8. Tim has seen some logs referencing complication 19.
    *   Push and hold a complication slot to bring up a menu of other complications to switch to.
    *   Quick tap the bounding area to access related screen e.g. battery setting.

**Relevant links:**

*   **_Jaye’s links:_**
    *   [https://developer.android.com/training/wearables/complication](https://developer.android.com/training/wearables/complication)
    *   Complications UI/UX overview  ([https://developer.android.com/design/ui/wear/guides/m2-5/surfaces/complications](https://developer.android.com/design/ui/wear/guides/m2-5/surfaces/complications))
    *   ([https://developer.android.com/training/wearables/watch-faces/adding-complications](https://developer.android.com/training/wearables/watch-faces/adding-complications))
    *   Complication elements (e.g. SHORT\_TEXT) ([https://developer.android.com/reference/wear-os/wff/complication/complication?version=4](https://developer.android.com/reference/wear-os/wff/complication/complication?version=4))
    *   Bounding areas ([https://developer.android.com/reference/wear-os/wff/complication/bounding?version=4](https://developer.android.com/reference/wear-os/wff/complication/bounding?version=4))
    *   ADD LINK FOR API CALLS IN COMPLICATIONS (PLACEHOLDER)
*   **_GitHub repositories with examples:_**
    *   [https://github.com/android/snippets/blob/bbf4e1ff2570641546d50270b121493ef1965774/watchface/src/main/res/raw/watchface\_complications.xml](https://github.com/android/snippets/blob/bbf4e1ff2570641546d50270b121493ef1965774/watchface/src/main/res/raw/watchface_complications.xml)
    *   https://github.com/android/wear-os-samples/blob/main/WatchFaceFormat/Complications/watchface/src/main/res/raw/watchface.xml
    *   [https://github.com/android/wear-os-samples](https://github.com/android/wear-os-samples) for overall repo
    *   If the complication attempts to retrieve data for a type it doesn't support, it ends up getting the default value for the type (text is null, numbers are 0, etc.):
    *   [https://developer.android.com/training/wearables/watch-faces/adding-complications#types-fields](https://developer.android.com/training/wearables/watch-faces/adding-complications#types-fields)
*   **_List of values for setting the defaultSystemProvider value to complications:_**
    *   https://developer.android.com/reference/wear-os/wff/complication/default-provider-policy?hl=en&version=4#bounding-arc-required-attributes
