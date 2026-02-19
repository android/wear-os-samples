## Complications Watch Face

The complications watch face demonstrates how to use [`ComplicationSlot`s][1] within the Watch Face Format to
host [complications][2].

### Configuring the watch face using ADB

The watch face can be configured using ADB as follows:

```shell
adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE \
    --es operation set-complication \
    --ecn component <component_name> \
    --es watchFaceId 'com.example.complications' \
    --ei slot <slot_id> \
    --ei type <complication_type>
```

If configuring with the [Complications data source sample][3], useful component names are:

```
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.GoalProgressDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.IconDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.LargeImageDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.LongTextDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.RangedValueDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.ShortTextDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.SmallImageDataSourceService
com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.WeightedElementsDataSourceService
```

Useful complication types are:

| Type | ID |
| :---- | :---- |
| GOAL\_PROGRESS | 13 |
| ICON | 6 |
| LARGE\_IMAGE | 8 |
| LONG\_TEXT | 4 |
| RANGED\_VALUE | 5 |
| SHORT\_TEXT | 3 |
| SMALL\_IMAGE | 7 |
| WEIGHTED\_ELEMENTS | 14 |

The complication slot IDs are:

| Complication | Slot ID |
| :---- | :---- |
| Background | 11 |
| Top | 12 |
| Left | 13 |
| Right | 14 |
| Bottom | 15 |

Example: Setting the top complication to show SHORT\_TEXT from the sample data sources:

```shell
adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE \
   --es operation set-complication \
   --ecn component 'com.example.android.wearable.wear.complications/com.example.android.wearable.wear.complications.ShortTextDataSourceService' \
    --es watchFaceId 'com.example.complications' \
    --ei slot 12 \
    --ei type 3
```

[1]: https://developer.android.com/reference/wear-os/wff/complication/complication-slot
[2]: https://developer.android.com/training/wearables/wff/complications
[3]: https://github.com/android/wear-os-samples/tree/main/Complications