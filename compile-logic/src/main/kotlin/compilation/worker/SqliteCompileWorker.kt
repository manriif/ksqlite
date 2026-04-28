package compilation.worker

import compilation.SqliteCompilationParameters
import compilation.SqliteTarget
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * Compiles a library.
 */
abstract class SqliteCompileWorker : WorkAction<SqliteCompileWorker.Parameters> {

    @get:Inject
    protected abstract val execOperations: ExecOperations

    ///////////////////////////////////////////////////////////////////////////
    // Parameters
    ///////////////////////////////////////////////////////////////////////////

    interface Parameters : WorkParameters {
        val compilationParameters: Property<SqliteCompilationParameters>
        val sourceFiles: ConfigurableFileCollection
        val target: Property<SqliteTarget>
    }
}