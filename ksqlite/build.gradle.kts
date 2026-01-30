import compilation.SqliteStaticTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import tasks.SqliteCompileStaticTask
import tasks.registerSqliteCompileStaticTask
import tasks.registerSqliteGenerateCInteropDefTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    /*val sqliteDirectory = layout.buildDirectory.dir("sqlite")
    val nativeArtifactDirectory = sqliteDirectory.map { it.dir("native") }
    val compileStaticTaskProvider = registerSqliteCompileStaticTask()

    listOf(linuxX64()).forEach {
        it.configureCInterop(nativeArtifactDirectory, compileStaticTaskProvider)
    }*/

    androidJvmTargets()
    //jvmTargets()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.cinterop.BetaInteropApi")
            }
        }

        androidMain.dependencies {
            implementation(projects.ksqliteJni)
        }
    }
}

/*fun KotlinNativeTarget.configureCInterop(
    artifactDirectory: Provider<Directory>,
    compileStaticTask: TaskProvider<SqliteCompileStaticTask>
) {
    val extension = ksqliteCompilerExtension
    val outputDirectoryProvider = artifactDirectory.map { it.dir(konanTarget.name) }
    val libraryDirectoryProvider = outputDirectoryProvider.map { it.dir("library") }

    val staticTarget = objects.newInstance<SqliteStaticTarget>().apply {
        this.konanTarget = this@configureCInterop.konanTarget
        this.libraryDirectory = libraryDirectoryProvider
    }

    val defFileProvider = outputDirectoryProvider.map { directory ->
        directory.file("${extension.sqliteCompilationParameters.get().sqliteName}.def")
    }

    val generateCInteropDefTaskProvider = registerSqliteGenerateCInteropDefTask(
        packageName = localNamespace,
        staticTarget = staticTarget,
        defFileProvider = defFileProvider
    )

    compileStaticTask.configure {
        targets.add(staticTarget)
    }

    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        cinterops.register("ksqlite") {
            tasks.named(interopProcessingTaskName).configure {
                dependsOn(generateCInteropDefTaskProvider)
                dependsOn(compileStaticTask)
            }

            definitionFile = defFileProvider
            extraOpts += listOf("-Xccall-mode", "direct")
            includeDirs(extension.sqliteSourcesDirectory)
        }
    }
}*/