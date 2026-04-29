@file:Suppress("HasPlatformType")

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import tasks.registerSqliteCompileStaticTask
import tasks.registerSqliteGenerateCInteropDefTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

val ksqliteDirectory = layout.buildDirectory.dir("ksqlite")
val nativeArtifactDirectory = ksqliteDirectory.map { it.dir("native") }
val sqliteCompileStaticXcodeTaskProvider = registerSqliteCompileStaticTask("Xcode")

val sqliteCompileStaticAndroidTaskProvider = registerSqliteCompileStaticTask("Android") {
    dependsOn(androidToolchainInstallTaskProvider)
}

val sqliteCompileStaticZigTaskProvider = registerSqliteCompileStaticTask("Zig") {
    // TODO zig dependency
}

val sqliteCompileStaticTaskProviders = mapOf(
    Family.ANDROID to sqliteCompileStaticAndroidTaskProvider,
    Family.OSX to sqliteCompileStaticXcodeTaskProvider,
    Family.IOS to sqliteCompileStaticXcodeTaskProvider,
    Family.TVOS to sqliteCompileStaticXcodeTaskProvider,
    Family.WATCHOS to sqliteCompileStaticXcodeTaskProvider,
    Family.LINUX to sqliteCompileStaticZigTaskProvider,
    Family.MINGW to sqliteCompileStaticZigTaskProvider,
)

kotlin {
    listOf(macosX64(), macosArm64()).forEach { target ->
        target.configureNativeTarget()
    }
}

fun KotlinNativeTarget.configureNativeTarget() {
    val extension = ksqliteExtension
    val platform = konanTarget.toPlatform()
    val platformDirectory = nativeArtifactDirectory.map { it.dir(platform.name) }

    val sqliteTarget = objects.newInstance<SqliteTarget>().apply {
        this.platform = platform

        this.libraryFile = platformDirectory.zip(extension.sqliteComponents) { dir, params ->
            dir.file(platform.operatingSystem.library.staticLibraryFileName(params.libraryName))
        }
    }

    val defFile = platformDirectory.map { directory ->
        directory.file("${extension.sqliteComponents.get().sqliteName}.def")
    }

    val generateCInteropDefTaskProvider = registerSqliteGenerateCInteropDefTask(
        packageName = projectNamespace,
        targetName = name,
        target = sqliteTarget,
        defFile = defFile
    )

    val sqliteCompileStaticTaskProvider = checkNotNull(
        sqliteCompileStaticTaskProviders[konanTarget.family]
    )

    sqliteCompileStaticTaskProvider.configure {
        targets.add(sqliteTarget)
    }

    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(sqliteCompileStaticTaskProvider)
        }

        cinterops.register("ksqlite") {
            tasks.named(interopProcessingTaskName).configure {
                dependsOn(sqliteCompileStaticTaskProvider)
                dependsOn(generateCInteropDefTaskProvider)
            }

            definitionFile = defFile
            extraOpts += listOf("-Xccall-mode", "direct")

            includeDirs(
                extension.ksqliteDirectory,
                extension.sqliteDirectory
            )
        }
    }
}