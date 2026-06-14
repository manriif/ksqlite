import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

private const val PREPARE_KOTLIN_IDEA_IMPORT = "prepareKotlinIdeaImport"
private const val PREPARE_KOTLIN_BUILD_SCRIPT_MODEL = "prepareKotlinBuildScriptModel"

/**
 * Tries to register the task for execution during IDE sync.
 */
fun Project.registerTaskForIde(provider: TaskProvider<*>) {
    tasks.findByName(PREPARE_KOTLIN_IDEA_IMPORT)
        ?.dependsOn(provider)
        ?: afterEvaluate {
            tasks.findByName(PREPARE_KOTLIN_IDEA_IMPORT)
                ?.dependsOn(provider)
                ?: rootProject.tasks.findByName(PREPARE_KOTLIN_BUILD_SCRIPT_MODEL)
                    ?.dependsOn(provider)
        }
}