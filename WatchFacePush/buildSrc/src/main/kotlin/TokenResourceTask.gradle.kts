/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import kotlin.io.readText
import kotlin.io.writeText
import kotlin.text.trimMargin

/**
 * Task to create a resource file containing the validation token of the default watch face.
 */
abstract class TokenResourceTask : DefaultTask() {
    @get:Input
    abstract val buildVariant: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: RegularFileProperty

    @TaskAction
    fun performAction() {
        val tokenFile =
            project.layout.buildDirectory.file("intermediates/watchfaceAssets/${buildVariant.get()}/default_watchface_token.txt")
                .get()
        val outputFile = outputDirectory.get().asFile.resolve("res/values/wf_token.xml")
        project.mkdir(outputFile.parent)
        val tokenResText = """<resources>
                         |    <string name="default_wf_token">${tokenFile.asFile.readText()}</string>
                         |</resources>
                       """.trimMargin()
        outputFile.writeText(tokenResText)
    }
}
