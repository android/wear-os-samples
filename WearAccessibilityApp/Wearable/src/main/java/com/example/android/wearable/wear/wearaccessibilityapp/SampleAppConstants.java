/*
 * Copyright (C) 2017 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.wear.wearaccessibilityapp;

public class SampleAppConstants {
    /** {@value #NORMAL} Used when sending information to an Adapter class */
    public static final int NORMAL = 1;

    /** {@value #HEADER_FOOTER} Used when sending information to an Adapter class */
    public static final int HEADER_FOOTER = 2;

    /** {@value #PROGRESS_BAR} Used when sending information to an Adapter class */
    public static final int PROGRESS_BAR = 3;

    /** {@value #SWITCH} Used when sending information to an Adapter class */
    public static final int SWITCH = 4;

    /** {@value #TITLE} Used when sending information to an Adapter class */
    public static final int TITLE = 5;

    /**
     * {@value #END_OF_LONG_LIST} Used to check if at the end of long list Used in
     * LongListRecyclerviewAdapter
     */
    public static final int END_OF_LONG_LIST = 45;
}
