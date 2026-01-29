import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import compilation.SqliteCompilationParameters
import org.gradle.internal.os.OperatingSystem
import java.util.Properties

plugins {
    alias(libs.plugins.android.multiplatformLibrary) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka) apply false // true
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.gradle.pluginPublish) apply false
    alias(libs.plugins.sqliteCompiler)
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.ksqlite.get()
}

sqliteCompiler {
    val (major, minor, patch, checksum) = libs.versions.sqliteMultipleCiphers.get().split('.')

    sqliteCompilationParameters = SqliteCompilationParameters(
        sqliteVersion = libs.versions.ksqlite.get(),
        sqliteMCVersion = "$major.$minor.$patch",
        androidNdkToolchainPath = androidNdkToolchainPath(),
        androidSdkMin = libs.versions.android.sdk.min.get(),
        macosVersionMin = libs.versions.macos.version.min.get(),
        iosVersionMin = libs.versions.ios.version.min.get(),
        tvosVersionMin = libs.versions.tvos.version.min.get(),
        watchosVersionMin = libs.versions.watchos.version.min.get()
    )

    sqliteDownloadChecksum = checksum
    sqliteDownloadDirectory = layout.buildDirectory.dir("tmp/sqlite")
    sqliteSourcesDirectory = layout.buildDirectory.dir("sqlite")
}

/**
 * Returns the path to the Android NDK toolchain
 */
fun androidNdkToolchainPath(): String {
    val ndkDir = androidNdkDirectory().absolutePath

    val ndkHostTag = OperatingSystem.current().run {
        when {
            isWindows -> "windows-x86_64"
            isMacOsX -> "darwin-x86_64"
            isLinux -> "linux-x86_64"
            else -> throw UnsupportedOperationException("Unsupported operation system $this")
        }
    }

    return "$ndkDir/toolchains/llvm/prebuilt/$ndkHostTag"
}

/**
 * Returns the Android NDK location or throws an exception.
 */
fun androidNdkDirectory(): File {
    // Try to find via ANDROID_HOME/ANDROID_SDK_ROOT
    val sdkRoot = rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.run {
            Properties().run {
                inputStream().use { load(it) }
                getProperty("sdk.dir")
            }
        }
        ?: System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")

    if (sdkRoot != null) {
        val ndkDir = File("$sdkRoot/ndk/${libs.versions.android.ndk.get()}")

        if (ndkDir.exists()) {
            return ndkDir
        }
    }

    // Try environment variables first
    val fromEnv = System.getenv("ANDROID_NDK_HOME")
        ?: System.getenv("ANDROID_NDK_ROOT")
        ?: System.getenv("NDK_HOME")

    if (fromEnv != null) {
        return File(fromEnv)
    }

    val extension = extensions.findByType<KotlinMultiplatformAndroidComponentsExtension>()

    return extension?.sdkComponents?.ndkDirectory?.get()?.asFile ?: error(
        "Android NDK not found. Set ANDROID_NDK_HOME environment variable or install NDK via" +
                " Android Studio"
    )
}