@file:Suppress("HasPlatformType")

import compilation.SqliteTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import platform.Architecture
import platform.OperatingSystem
import platform.Platform
import tasks.SqliteCompileStaticTask
import tasks.registerSqliteCompileStaticTask
import tasks.registerSqliteGenerateCInteropDefTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

val sqliteDirectory = layout.buildDirectory.dir("sqlite")
val nativeArtifactDirectory = sqliteDirectory.map { it.dir("native") }
val compileStaticTaskProvider = registerSqliteCompileStaticTask()

kotlin {
    /*listOf(androidNativeX64()).forEach {
        it.configureNativeTarget(nativeArtifactDirectory, compileStaticTaskProvider)
    }*/

    jvmTargets()
}

fun KotlinNativeTarget.configureNativeTarget(
    artifactDirectory: Provider<Directory>,
    compileStaticTask: TaskProvider<SqliteCompileStaticTask>
) {
    val extension = ksqliteCompilerExtension
    val outputDirectoryProvider = artifactDirectory.map { it.dir(konanTarget.name) }
    val libraryDirectoryProvider = outputDirectoryProvider.map { it.dir("library") }

    val compilationTarget = objects.newInstance<SqliteTarget>().apply {
        this.platform = konanTarget.toPlatform()
        this.libraryDirectory = libraryDirectoryProvider
    }

    val defFileProvider = outputDirectoryProvider.map { directory ->
        directory.file("${extension.compilationParams.get().sqliteName}.def")
    }

    val generateCInteropDefTaskProvider = registerSqliteGenerateCInteropDefTask(
        packageName = projectNamespace,
        target = compilationTarget,
        defFileProvider = defFileProvider
    )

    compileStaticTask.configure {
        targets.add(compilationTarget)
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
}

/**
 * Transforms `this` [KonanTarget] into [Platform].
 */
fun KonanTarget.toPlatform(): Platform = when (this) {
    KonanTarget.ANDROID_ARM32 -> Platform(OperatingSystem.Android, Architecture.Arm32)
    KonanTarget.ANDROID_ARM64 -> Platform(OperatingSystem.Android, Architecture.Arm64)
    KonanTarget.ANDROID_X64 -> Platform(OperatingSystem.Android, Architecture.X64)
    KonanTarget.ANDROID_X86 -> Platform(OperatingSystem.Android, Architecture.X86)
    KonanTarget.IOS_ARM64 -> Platform(OperatingSystem.IOS.Device, Architecture.Arm64)
    KonanTarget.IOS_SIMULATOR_ARM64 -> Platform(OperatingSystem.IOS.Simulator, Architecture.Arm64)
    KonanTarget.IOS_X64 -> Platform(OperatingSystem.IOS.Simulator, Architecture.X64)
    KonanTarget.LINUX_ARM64 -> Platform(OperatingSystem.Linux, Architecture.Arm64)
    KonanTarget.LINUX_X64 -> Platform(OperatingSystem.Linux, Architecture.X64)
    KonanTarget.MACOS_ARM64 -> Platform(OperatingSystem.MacOS, Architecture.Arm64)
    KonanTarget.MACOS_X64 -> Platform(OperatingSystem.MacOS, Architecture.X64)
    KonanTarget.MINGW_X64 -> Platform(OperatingSystem.Windows, Architecture.X64)
    KonanTarget.TVOS_ARM64 -> Platform(OperatingSystem.TvOS.Device, Architecture.Arm64)
    KonanTarget.TVOS_SIMULATOR_ARM64 -> Platform(OperatingSystem.TvOS.Simulator, Architecture.Arm64)
    KonanTarget.TVOS_X64 -> Platform(OperatingSystem.TvOS.Simulator, Architecture.X64)
    KonanTarget.WATCHOS_ARM32 -> Platform(OperatingSystem.WatchOS.Device, Architecture.Arm32)
    KonanTarget.WATCHOS_ARM64 -> Platform(OperatingSystem.WatchOS.Device, Architecture.Arm64)

    KonanTarget.WATCHOS_DEVICE_ARM64 ->
        Platform(OperatingSystem.WatchOS.DeviceGen2, Architecture.Arm64)

    KonanTarget.WATCHOS_SIMULATOR_ARM64 ->
        Platform(OperatingSystem.WatchOS.Simulator, Architecture.Arm64)

    KonanTarget.WATCHOS_X64 -> Platform(OperatingSystem.WatchOS.Simulator, Architecture.X64)
    KonanTarget.LINUX_ARM32_HFP -> error("Unsupported compilation target: $this")
}