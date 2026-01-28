import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()

    listOf(macosX64()).forEach { nativeTarget ->
        nativeTarget.configureCInterop()
    }

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

fun KotlinNativeTarget.configureCInterop() {
    val targetName = name.uppercaseFirstChar()
    val compiler = sqliteCompiler

    val compileSqliteTask = compiler.registerSqliteCompilationTask(
        nativeTarget = this,
        targetName = targetName,
        packageName = localNamespace
    )

    val generatedDefinitionFile = compiler.run {
        sqliteNativeLibDirectory.zip(sqliteRelease) { directory, release ->
            directory.file("${konanTarget.name}/${release.sqliteName}.def")
        }
    }

    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        cinterops.register("ksqlite") {
            tasks.named(interopProcessingTaskName).configure {
                dependsOn(compileSqliteTask)
            }

            definitionFile = generatedDefinitionFile
            includeDirs(sqliteCompiler.sourceTask.map { it.outputs.files })
        }
    }
}