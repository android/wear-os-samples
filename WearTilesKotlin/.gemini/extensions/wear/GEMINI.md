# GEMINI.md

## Rules and Requirements

### General

- After any file modification, run the `build-apk` tool to check for and resolve compilation errors.
- Work with tool-generated files non-destructively. For example, use `cp` instead of `mv`.
- Only take a screenshot if specifically requested or if it's essential for completing a task.

### Protolayout and Modifiers

- **Use Material 3:** Prioritize functions from the `androidx.wear.protolayout.material3` package.
  Avoid using the `androidx.wear.protolayout.material` library unless absolutely necessary.
- **Use `LayoutModifier` Functions:** Use `LayoutModifier.*` functions directly instead of
  `Modifiers.Builder().*`. Use `.toProtoLayoutModifiers()` for type conversions when needed.
- **Understand Tile Modifiers:** Be aware that Tiles modifiers are distinct from Compose or Wear
  Compose modifiers. Ensure you are generating the correct modifier code for Tiles.
- **Layout Weight:** `LayoutModifier` does not have a `weight()` function. To set widths for
  multiple elements, place them in a `Box` and use `.setWidth(weight(0.7f))`.
- **Colors:** Do not explicitly pass the color scheme to button color functions (e.g.,
  `ButtonDefaults.filledVariantButtonColors(colorScheme.primary)`). Instead, use the default
  functions like `filledVariantButtonColors()` from
  `androidx.wear.protolayout.material3.ButtonDefaults`.
- **Scope Functions:** Prefer `MaterialScope.*` scope functions over `Builder()` style
  constructions.

### Tool Usage

- **Use ADB Tools:** Utilize the provided tools whenever possible, even if direct command execution
  seems simpler.
  - Do not run `./gradlew` directly.
  - Use the `list-tiles` tool to find a tile. Only resort to parsing `AndroidManifest.xml` if the
    tool fails.
  - If `adb` commands fail, run `get-state` to verify device status. When a device is connected and
    ready to receive commands, the output will be the string `device`.

### Code Style

- **Use Helper Functions:** Review `./app/src/main/java/com/example/wear/tiles/tools/Extensions.kt`
  for existing helper functions. Feel free to add similar functions to improve code readability.

## Useful Resources

- **Library Source Code:** The full source code for the Tiles and ProtoLayout libraries is available
  in the `.context/` directory.

  - **ProtoLayout:** Example: The source for `androidx.wear.protolayout.material3.iconEdgeButton` is
    in `.context/proto/androidx/wear/protolayout/material3/EdgeButton.kt`.
  - **Tiles:** Example: The source for `android.wear.tiles.TileService` is in
    `.context/tiles/androidx/wear/tiles/TileService.java`.

- **Layout Examples:** The `.context/samples` directory contains syntactically correct layout code
  examples. These can be consulted for layout implementation details, even though they lack the
  supporting service infrastructure.

- **Visual Verification:** To verify the visual impact of a code change, you can take a screenshot
  using a tool and ask for an analysis. For example: "Is the text in `/tmp/screenshot-AAAAA.png`
  larger than in `/tmp/screenshot-BBBBB.png`?"

- **Golden Tiles:** The "golden" Tiles are part of the "debug" build variant and have their own
  manifest at `./app/src/debug/AndroidManifest.xml`.

## Reference Information

### Tile Previews

Each Tile Service must be associated with a static preview image that is displayed to the user in
the tile carousel on a Wear OS device. This association is configured within the
`AndroidManifest.xml` file.

#### How It Works

1. **Service Declaration**: In Android, every tile is implemented as a `Service`. To be recognized
   by the system as a tile provider, the service declaration in the manifest must include an
   `intent-filter` for the `androidx.wear.tiles.action.BIND_TILE_PROVIDER` action.

2. **Preview Metadata**: Inside the `<service>` declaration, a `<meta-data>` tag with
   `android:name="androidx.wear.tiles.PREVIEW"` is used to specify the tile's preview image. The
   `android:resource` attribute of this tag points to a drawable resource (e.g.,
   `@drawable/tile_preview_weather`).

#### Example `AndroidManifest.xml` Entry

```xml
<!-- AndroidManifest.xml -->

<service android:name=".golden.WeatherTileService" android:label="@string/tile_label_weather"
  android:permission="com.google.android.wearable.permission.BIND_TILE_PROVIDER"
  android:exported="true">

  <!-- Identifies the service as a Tile Provider -->
  <intent-filter>
    <action android:name="androidx.wear.tiles.action.BIND_TILE_PROVIDER" />
  </intent-filter>

  <!-- Links the Tile Provider to a static drawable for its preview -->

  <meta-data android:name="androidx.wear.tiles.PREVIEW"
    android:resource="@drawable/tile_preview_weather" />

</service>
```

#### Responsive Previews for Different Screen Sizes

You can provide different preview images for different device configurations using Android's
standard resource qualifier system. For instance, to supply a distinct preview for watches with
larger screens, you can place an alternative version of the drawable in a `res/drawable-w225dp/`
directory. The system automatically selects the most appropriate resource based on the device's
characteristics.

A screen/display is considered "large" if its size in Dp is >=225.

## Processes and Guides

### How to Regenerate or Update Tile Previews

Note: only do this is specifically asked!

1. **Identify Tile Services** Determine the application's tile services using one of the following
   methods:

   - **Method A (Static Analysis):** Review the `app/src/main/AndroidManifest.xml` file to locate
     all `<service>` declarations that include an intent filter for the `BIND_TILE_PROVIDER` action.

   - **Method B (Live Query):** Install the application on a device and run the following `adb`
     command, replacing `your.package.name` with the app's package name. This queries the device for
     all installed tile provider services belonging to your app.

     ```shell
     adb exec-out cmd package query-services -a \
     androidx.wear.tiles.action.BIND_TILE_PROVIDER --brief | \
     grep your.package.name | \
     sort
     ```

1. **Build and Install** If not already done, compile the latest version of the application and
   install it onto a connected Wear OS device or emulator.

1. **Generate and Update Previews for Each Tile** For each tile service identified in Step 1,
   perform the following sequence:

   a. **Check for an Existing Preview** In `AndroidManifest.xml`, inspect the `<service>` entry. If
   a `<meta-data>` tag with the name `androidx.wear.tiles.PREVIEW` already exists, note the drawable
   name specified in its `android:resource` attribute (e.g., `@drawable/tile_preview_existing`).
   This is the file you will overwrite.

   b. **Add the Tile to the Device** Use the appropriate developer tool to add an instance of the
   tile to the device's tile carousel.

   c. **Display the Tile** Navigate the device's UI to bring the newly added tile into full view on
   the screen.

   d. **Take a Screenshot** Capture a screenshot of the device's display.

   e. **Save or Overwrite the Preview Image** Save the screenshot into the project's
   `app/src/main/res/drawable/` directory.

   - If an existing preview file was identified in step 3a, overwrite it with the new screenshot.
   - Otherwise, save the screenshot under a new, descriptive name (e.g.,
     `tile_preview_servicename.png`).

   f. **Update the Manifest (if necessary)**

   - If you overwrote an existing file, no manifest change is needed.
   - If you created a new file, add the corresponding `<meta-data>` tag to the `<service>`
     declaration, linking to the new drawable resource.

   g. **Clean Up** Remove the tile instance from the device's carousel.

1. **Verify Changes** After processing all tiles, rebuild the application to confirm that the
   manifest is correct and all preview resources are included properly.
