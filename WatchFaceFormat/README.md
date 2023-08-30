# Watch Face Format Sample

A sample demonstrating the structure of Watch Face Format watch faces

## Introduction

The Watch Face Format (WFF) allows developers to build watch faces, and watch
face design tools in a XML format.

This sample demonstrates the basic structure of a WFF watch face, and how it
should be packaged for uploading to Play or for local testing on an emulator or
device.

For more details on the Watch Face Format, see:

- [Watch Face Format overview][wff-overview]
- [Watch Face Format setup][wff-setup]
- [XML reference][wff-xml-reference]
- [Design guidance][watch-face-design-guidance]

## Building the sample

This sample uses a shell script to demonstrate the packaging of the watch face.

You will need to set a handful of environment variables first:

- `ANDROID_HOME` - path to your SDK installation, for example on Mac:
  `/Users/<my_user>/Library/Android/sdk`
- `AAPT2` - path to the AAPT2 binary, for example:
  `$ANDROID_HOME/build-tools/<version>/aapt2`
- `ANDROID_JAR` - path to the Android JAR, for example:
  `$ANDROID_HOME/platforms/android-<version>/android.jar`
- `BUNDLETOOL` - path to the Bundle Tool. On Mac, this can be installed using
  `brew install bundletool`

From the `WatchFaceFormat` directory, execute:

```shell
./build-wff.sh SimpleDigital
```

This will build the watch face in a few formats, notably:

- `SimpleDigital/out/mybundle.aab` - a bundle suitable for upload to Play
- `SimpleDigital/out/result_apks/universal.apk` - an APK, for easy deployment
  to a local device.

## Deploying locally for test

On a Wear 4 device, such as the Wear emulator:

1. install the watch face:

    ```shell
    adb install SimpleDigital/out/result_apks/universal.apk
    ```

1. Long press on the current watch face, and locate the option to select further
   watch faces. Choose the **Simple Digital** watch face.

## Support

- Stack Overflow: <https://stackoverflow.com/questions/tagged/wear-os>
- Error Reporting: If you've found an error in this sample, please file an
  issue: <https://github.com/android/wear-os-samples>
- Submitting Patches: Patches are encouraged, and may be submitted by forking
  this project and submitting a pull request through GitHub. Please see
  [CONTRIBUTING.md][contributing] for more details.

[wff-overview]: https://developer.android.com/training/wearables/wff
[wff-setup]: https://developer.android.com/training/wearables/wff/setup
[wff-xml-reference]: https://developer.android.com/training/wearables/wff/watch-face
[watch-face-design-guidance]: https://developer.android.com/design/ui/wear/guides/surfaces/watch-faces
[contributing]: ../CONTRIBUTING.md
