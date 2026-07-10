import komple.gradle.project.c.CLibrary
import komple.platform.Platform
import komple.project.c.CLibraryType
import modules.ksqliteFfmResourceLibDirectory
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import tasks.GenerateFfmSourcesTask

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val generatedSourceDirectory = layout.buildDirectory.dir("generated/ksqlite/src/jvmMain")

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

val generateFfmSources by tasks.registeringKsqlite<GenerateFfmSourcesTask> {
    outputDirectory = generatedSourceDirectory.map { it.dir("kotlin") }
    cProject = komple.projects.kotlinSqlite.kProject
    compilations = libraries.map(CLibrary::compilation)
}

val copyJavaBindings by tasks.registeringKsqlite<Copy> {
    from(javaBindings.map { it.generateDirectory })
    into(generatedSourceDirectory.map { it.dir("java") })
}

val generateSources by tasks.registeringKsqlite {
    dependsOn(generateFfmSources)
    dependsOn(copyJavaBindings)
}

registerTaskForIde(generateSources)

kotlin {
    jvmTargets().forEach { target ->
        target.configureJvmTarget()
    }

    sourceSets.jvmMain {
        kotlin.srcDirs(generateFfmSources, copyJavaBindings)
    }
}

fun KotlinJvmTarget.configureJvmTarget() {
    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        checkNotNull(compileJavaTaskProvider).configure {
            source(copyJavaBindings)
        }

        tasks.named<ProcessResources>(processResourcesTaskName).configure {
            libraries.forEach { library ->
                from(library.libraryFile) {
                    into(library.compilation.platform.map { platform ->
                        platform.ksqliteFfmResourceLibDirectory()
                    })
                }
            }
        }
    }
}