import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    val sqliteDirectory = layout.buildDirectory.dir("sqlite")
    val nativeArtifactDirectory = sqliteDirectory.map { it.dir("native") }
    val compileAllTask = registerSqliteCompileAllNativeTargetsTask()

    listOf(macosX64()).forEach {
        it.configureCInterop(nativeArtifactDirectory, compileAllTask)
    }

    androidJvmTargets()

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

fun KotlinNativeTarget.configureCInterop(
    artifactDirectory: Provider<Directory>,
    compileAllTask: TaskProvider<*>
) {
    val compiler = sqliteCompiler
    val outputDirectory = artifactDirectory.map { it.dir(konanTarget.name) }

    // Use the output from source task to force implicit dependency
    val libraryDirectoryProvider = outputDirectory.map { it.dir("library") }

    val defFileProvider = outputDirectory.map { directory ->
        directory.file("${compiler.sqliteRelease.get().sqliteName}.def")
    }

    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        cinterops.register("ksqlite") {
            tasks.named(interopProcessingTaskName).configure {
                dependsOn(compileAllTask)
            }

            definitionFile = defFileProvider
            includeDirs(compiler.sqliteSourcesDirectory)
        }
    }

    val compileTask = compiler.registerSqliteCompileNativeTargetTask(
        nativeTarget = this,
        packageName = localNamespace,
        libraryDirectoryProvider = libraryDirectoryProvider,
        defFileProvider = defFileProvider
    )

    compileAllTask.dependsOn(compileTask)
}