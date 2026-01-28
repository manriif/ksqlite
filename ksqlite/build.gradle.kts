plugins {
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()
}

/*

val androidNdkPath = androidNdkPath()

fun KotlinNativeTarget.configureCInterop() {
    val targetName = name.uppercaseFirstChar()
    val compileSqliteTaskName = "compileSqlite$targetName"
    val archiveSqliteTaskName = "archiveSqlite$targetName"
    val nativeTargetDirectory = sqliteArtefactsNativeDirectory.map { it.dir(konanTarget.name) }
    val sourceFile = sqliteSourcesDirectory.map { it.file("$sqliteName.c") }.get().asFile
    val objectFile = nativeTargetDirectory.map { it.file("$sqliteName.o") }.get().asFile
    val artefactFile = nativeTargetDirectory.map { it.file("lib$sqliteName.a") }.get().asFile
    val compiler = getCompiler(konanTarget)
    val archiver = getArchiver(konanTarget)
    val compilerFlags = getCompilerFlags(konanTarget)

    val compileSqliteTaskProvider = tasks.register<Exec>(compileSqliteTaskName) {
        group = sqliteTaskGroup
        workingDir = layout.projectDirectory.asFile
        dependsOn(sqliteExtractSourcesTaskProvider)

        inputs.file(sourceFile)
        outputs.file(objectFile)

        doFirst {
            sqliteArtefactsNativeDirectory.get().asFile.mkdirs()
        }

        commandLine(
            compiler,
            *compilerFlags.toTypedArray(),
            "-c",
            sourceFile.absolutePath,
            "-o",
            objectFile.absolutePath,
            "-DSQLITE_ENABLE_FTS5",
            "-DSQLITE_ENABLE_RTREE",
            "-O2"
        )
    }

    val archiveSqliteTaskProvider = tasks.register<Exec>(archiveSqliteTaskName) {
        group = sqliteTaskGroup
        workingDir = layout.projectDirectory.asFile
        dependsOn(compileSqliteTaskProvider)

        inputs.file(objectFile)
        outputs.file(artefactFile)

        commandLine(
            archiver,
            "rcs",
            artefactFile.absolutePath,
            objectFile.absolutePath
        )
    }

    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        cinterops.register("ksqlite") {
            tasks.named(interopProcessingTaskName).configure {
                dependsOn(sqliteGenerateDefFileTaskProvider)
                dependsOn(archiveSqliteTaskProvider)
            }

            definitionFile = sqliteDefDirectory.map { it.file("$sqliteName.def") }
            includeDirs(sqliteSourcesDirectory)
        }
    }
}

@Suppress("DEPRECATION")
kotlin {
    androidJvmTargets()
    nativeTargets().firstOrNull()?.configureCInterop()
    //nativeTargets().forEach(KotlinNativeTarget::configureCInterop)

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.cinterop.BetaInteropApi")
            }
        }

        androidMain.dependencies {
            implementation(projects.ksqlite.sqliteAndroidJni)
        }
    }
}

fun xcodeSdkPath(sdk: String): String {
    val sdkDir = sdk.replaceFirstChar { it.uppercase() }

    return "/Applications/Xcode.app/Contents/Developer/Platforms/$sdkDir.platform" +
            "/Developer/SDKs/$sdkDir.sdk"
}

fun getCompilerFlags(target: KonanTarget): List<String> {
    return when (target.family) {
        Family.OSX -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported macOS architecture: ${target.architecture}")
            }
            listOf("-arch", arch, "-mmacosx-version-min=10.13")
        }

        Family.IOS -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported iOS architecture: ${target.architecture}")
            }

            val sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "iphonesimulator"
                else -> "iphoneos"
            }

            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mios-version-min=12.0")
        }

        Family.TVOS -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported tvOS architecture: ${target.architecture}")
            }

            val sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "appletvsimulator"
                else -> "appletvos"
            }

            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mtvos-version-min=12.0")
        }

        Family.WATCHOS -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> when (target.name) {
                    "watchosDeviceArm64" -> "arm64_32"
                    else -> "arm64"
                }

                Architecture.ARM32 -> "armv7k"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported watchOS architecture: ${target.architecture}")
            }

            val sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "watchsimulator"
                else -> "watchos"
            }

            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mwatchos-version-min=5.0")
        }

        Family.LINUX -> {
            val targetTriple = when (target.architecture) {
                Architecture.X64 -> "x86_64-unknown-linux-gnu"
                Architecture.ARM64 -> "aarch64-unknown-linux-gnu"
                else -> error("Unsupported Linux architecture: ${target.architecture}")
            }

            listOf("--target=$targetTriple")
        }

        Family.MINGW -> {
            // Only mingwX64 in your targets
            listOf("-target", "x86_64-w64-mingw32")
        }

        Family.ANDROID -> {
            val (targetTriple, apiLevel) = when (target.architecture) {
                Architecture.ARM64 -> "aarch64-linux-android" to 21
                Architecture.ARM32 -> "armv7a-linux-androideabi" to 21
                Architecture.X64 -> "x86_64-linux-android" to 21
                Architecture.X86 -> "i686-linux-android" to 21
            }

            listOf(
                "-target", "$targetTriple$apiLevel",
                "--sysroot=$androidNdkPath/toolchains/llvm/prebuilt/darwin-x86_64/sysroot"
            )
        }
    }
}

fun getCompiler(target: KonanTarget): String {
    return when (target.family) {
        Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> "clang"
        Family.LINUX -> "clang"
        Family.MINGW -> "x86_64-w64-mingw32-gcc"
        Family.ANDROID -> "$androidNdkPath/toolchains/llvm/prebuilt/darwin-x86_64/bin/clang"
    }
}

fun getArchiver(target: KonanTarget): String {
    return when (target.family) {
        Family.MINGW -> "x86_64-w64-mingw32-ar"
        Family.ANDROID -> "$androidNdkPath/toolchains/llvm/prebuilt/darwin-x86_64/bin/llvm-ar"
        else -> "ar"
    }
}

fun androidNdkPath(): String {
    // Try environment variables first
    val fromEnv = System.getenv("ANDROID_NDK_HOME")
        ?: System.getenv("ANDROID_NDK_ROOT")
        ?: System.getenv("NDK_HOME")

    if (fromEnv != null) {
        return fromEnv
    }

    // Try to find via ANDROID_HOME/ANDROID_SDK_ROOT
    val sdkRoot = System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: Properties().run {
            rootProject.file("local.properties").inputStream().use { load(it) }
            getProperty("sdk.dir")
        }

    if (sdkRoot != null) {
        val ndkDir = File("$sdkRoot/ndk/${libs.versions.android.ndk.get()}")

        if (ndkDir.exists()) {
            return ndkDir.absolutePath
        }
    }

    val extension = extensions.findByType<KotlinMultiplatformAndroidComponentsExtension>()

    return extension?.sdkComponents?.ndkDirectory?.get()?.asFile?.absolutePath ?: error(
        "Android NDK not found. Set ANDROID_NDK_HOME environment variable or install NDK via" +
                " Android Studio"
    )
}*/