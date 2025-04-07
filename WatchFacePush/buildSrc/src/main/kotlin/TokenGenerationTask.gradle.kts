
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.android.build.api.variant.BuiltArtifactsLoader
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import kotlin.collections.single
import kotlin.io.path.name
import kotlin.io.resolve
import kotlin.io.writeText
import kotlin.text.removeSuffix

/**
 * Task to generate a validation token for a given APK and write it to the specified location.
 */
abstract class TokenGenerationTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkLocation: DirectoryProperty

    @get:Internal
    abstract val artifactsLoader: Property<BuiltArtifactsLoader>

    @get:OutputDirectory
    abstract val tokenDirectory: RegularFileProperty

    @TaskAction
    fun performAction() {
        val artifacts =
            artifactsLoader.get().load(apkLocation.get())
                ?: throw GradleException("Cannot load APKs")
        if (artifacts.elements.size != 1)
            throw GradleException("Expected only one APK!")
        val apkPath = File(artifacts.elements.single().outputFile).toPath()

        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOperations.javaexec {
            classpath = project.rootProject.files("lib/DwfValidation_deploy.jar")
            args("--apk_path=$apkPath")

            standardOutput = stdOut
            errorOutput = stdErr
        }
        val tokenFileName = apkPath.name.removeSuffix(".apk") + "_token.txt"
        val tokenOutFile = tokenDirectory.get().asFile.resolve(tokenFileName)
                val match = Pattern.compile("generated token: (\\S+)").matcher(stdOut.toString())
                if (match.find()) {
                    val token = match.group(1)
                    tokenOutFile.writeText(token)
                } else {
                    throw TaskExecutionException(
                        this@TokenGenerationTask,
                        GradleException("No token generated for $tokenFileName")
                    )
                }
            }
    }
