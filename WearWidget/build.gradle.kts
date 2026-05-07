plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            val buildDir = layout.buildDirectory.get().asFile
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")

            ktfmt().kotlinlangStyle()
            licenseHeaderFile(rootProject.file("../spotless/copyright.kt"))
        }
        format("xml") {
            target("**/*.xml")
            targetExclude("**/build/**/*.xml")
            licenseHeaderFile(rootProject.file("../spotless/copyright.xml"), "(<[^!?])")
        }
    }
}