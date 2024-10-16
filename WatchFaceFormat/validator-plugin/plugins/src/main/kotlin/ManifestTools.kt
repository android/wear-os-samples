/*
 * Copyright 2024 The Android Open Source Project
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

import groovy.xml.Namespace
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.Node
import org.gradle.api.GradleException

const val WFF_PROP_NAME = "com.google.wear.watchface.format.version"

internal fun String.withNS(ns: Namespace) = "{${ns.uri}}$this"

/**
 * Obtains the Watch Face Format version from the AndroidManifest.xml, or throws an error if a valid
 * value cannot be found.
 */
internal fun getWffVersion(manifestPath: String): Int {
    val manifestXml =
        XmlSlurper(false, true).parse(manifestPath)
    val ns = Namespace("http://schemas.android.com/apk/res/android", "android")
    val applicationNode = manifestXml.getProperty("application") as GPathResult
    val versionProp = applicationNode.childNodes().asSequence().firstOrNull {
        val node = it as? Node
        node?.name() == "property" && WFF_PROP_NAME == node.attributes()?.get("name".withNS(ns))
    } as Node?
    if (versionProp == null) {
        throw GradleException("AndroidManifest.xml does not contain, or has invalid WFF version property")
    }
    val valueAttr = versionProp.attributes()?.get("value".withNS(ns))
        ?: throw GradleException("WFF version property does not have a value attribute")

    return valueAttr.toString().toIntOrNull()
        ?: throw GradleException("WFF version is not a valid integer")
}
