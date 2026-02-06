@file:Suppress("HasPlatformType")

import compilation.SqliteTarget
import compilation.staticLibraryFileName
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import platform.Architecture
import platform.OperatingSystem
import platform.Platform
import tasks.registerSqliteCompileStaticTask
import tasks.registerSqliteGenerateCInteropDefTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

val sqliteDirectory = layout.buildDirectory.dir("sqlite")
val nativeArtifactDirectory = sqliteDirectory.map { it.dir("native") }
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
    listOf(macosX64()).forEach { target ->
        target.configureNativeTarget()
    }
}

fun KotlinNativeTarget.configureNativeTarget() {
    val extension = ksqliteExtension
    val platform = konanTarget.toPlatform()
    val platformDirectory = nativeArtifactDirectory.map { it.dir(platform.name) }

    val sqliteTarget = objects.newInstance<SqliteTarget>().apply {
        this.platform = platform

        this.libraryFile = platformDirectory.zip(extension.compilationParams) { dir, params ->
            dir.file(platform.operatingSystem.library.staticLibraryFileName(params.libraryName))
        }
    }

    val defFile = platformDirectory.map { directory ->
        directory.file("${extension.compilationParams.get().sqliteName}.def")
    }

    val generateCInteropDefTaskProvider = registerSqliteGenerateCInteropDefTask(
        packageName = projectNamespace,
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
                dependsOn(generateCInteropDefTaskProvider)
            }

            definitionFile = defFile
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