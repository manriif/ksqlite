import komple.platform.Platform
import komple.project.c.CLibraryType
import modules.createKsqliteFfmRuntimeMetadataContent
import modules.ksqliteFfmResourceLibDirectory
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val generatedSourceDirectory = layout.buildDirectory.dir("generated/ksqlite/src/jvmMain")
val generatedJavaSourceDirectory = generatedSourceDirectory.map { it.dir("java") }
val generatedKotlinSourceDirectory = generatedSourceDirectory.map { it.dir("kotlin") }

val libraries = Platform.run {
    listOf(
        linuxArm64,
        linuxX64,
        macosArm64,
        macosX64,
        mingwArm64,
        mingwX64
    ).map { platform ->
        komple.projects.kotlinSqlite.createLibrary(CLibraryType.Shared, platform)
    }
}

val javaBindings by komple.projects.kotlinSqlite.jextract.bindingGenerators.registering {
    options {
        headerClassName = SQLITE3
        includeConstants = SqliteConstants
        includeFunctions = sqliteFunctions(true)
        includeStructs = SqliteStructs
        includeTypedefs = KsqliteTypedefs
    }
}

val generateFfmMetadata by tasks.registeringKsqlite {
    val cProject = komple.projects.kotlinSqlite.kProject
    val compilations = libraries.map { it.compilation }

    val metadataFile = generatedKotlinSourceDirectory.zip(cProject.packageName) { directory, name ->
        directory.file("$name/KsqliteFfmGenerated.kt")
    }

    outputs.file(metadataFile)

    doLast {
        metadataFile.writeContent(createKsqliteFfmRuntimeMetadataContent(cProject, compilations))
    }
}

val generateFfmSources by tasks.registeringKsqlite<Copy> {
    dependsOn(generateFfmMetadata)
    from(javaBindings.map { it.generateDirectory })
    into(generatedJavaSourceDirectory)
}

registerTaskForIde(generateFfmSources)

kotlin {
    jvmTargets().forEach { target ->
        target.configureJvmTarget()
    }

    sourceSets.jvmMain {
        kotlin.srcDirs(generatedJavaSourceDirectory, generatedKotlinSourceDirectory)
    }
}

fun KotlinJvmTarget.configureJvmTarget() {
    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(generateFfmSources)
        }

        checkNotNull(compileJavaTaskProvider).configure {
            source(generateFfmSources.map { it.destinationDir })
        }

        tasks.named<ProcessResources>(processResourcesTaskName).apply {
            configure {
                libraries.forEach { library ->
                    from(library.libraryFile) {
                        into(library.compilation.platform.ksqliteFfmResourceLibDirectory())
                    }
                }
            }
        }
    }
}