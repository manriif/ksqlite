import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure

private const val PREPARE_KOTLIN_IDEA_IMPORT = "prepareKotlinIdeaImport"
private const val PREPARE_KOTLIN_BUILD_SCRIPT_MODEL = "prepareKotlinBuildScriptModel"

/**
 * Registers the task as a dependency during Gradle sync when running in IDE.
 *
 * If no task exists for IDE and [onFailed] is supplied then [onFailed] is invoked after project
 * getting evaluated.
 */
fun Project.registerTaskForIde(
    provider: TaskProvider<*>,
    onFailed: (() -> Unit)? = null
) {
    tasks.all {
        if (name == PREPARE_KOTLIN_IDEA_IMPORT) {
            dependsOn(provider)
        }
    }

    onFailed?.let { callback ->
        afterEvaluate {
            if (tasks.none { it.name == PREPARE_KOTLIN_IDEA_IMPORT }) {
                rootProject.tasks.findByName(PREPARE_KOTLIN_BUILD_SCRIPT_MODEL)
                    ?.run { dependsOn(provider) }
                    ?: callback()
            }
        }
    }
}