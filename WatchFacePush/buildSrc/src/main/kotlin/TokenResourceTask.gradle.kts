
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

//    init {
//        outputDirectory.convention(project.layout.buildDirectory.dir("generated/wfTokenRes/"))
//        outputDirectory.con()
//    }

    @TaskAction
    fun performAction() {
        val tokenFile =
            project.layout.buildDirectory.file("intermediates/watchfaceAssets/${buildVariant.get()}/default_watchface_token.txt")
                .get()
        val outputFile = outputDirectory.get().asFile.resolve("${buildVariant.get()}/res/values/wf_token.xml")
        project.mkdir(outputFile.parent)
        val tokenResText = """<resources>
                         |    <string name="default_wf_token">${tokenFile.asFile.readText()}</string>
                         |</resources>
                       """.trimMargin()
        outputFile.writeText(tokenResText)
    }
}
