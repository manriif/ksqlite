import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

private const val PREPARE_KOTLIN_IDEA_IMPORT = "prepareKotlinIdeaImport"

/**
 * Registers the task as a dependency during Gradle sync when running in IDE.
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
                callback()
            }
        }
    }
}