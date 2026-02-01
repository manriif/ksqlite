import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import tasks.registerJextractGenerateBindingsTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

val generatedJavaSourceDirectory: Provider<Directory> = layout.buildDirectory.map { directory ->
    directory.dir("generated/ksqlite/src/jvmMain/java")
}

val generateBindingsTaskProvider = registerJextractGenerateBindingsTask(
    packageName = projectNamespace,
    outputDirectory = generatedJavaSourceDirectory
)

registerTaskForIde(generateBindingsTaskProvider)

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.target.ffm.get())
    }

    jvmTargets(libs.versions.jvm.target.ffm).forEach { target ->
        target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
            checkNotNull(compileJavaTaskProvider).configure {
                dependsOn(generateBindingsTaskProvider)
            }
        }
    }

    sourceSets.jvmMain {
        kotlin.srcDir(generatedJavaSourceDirectory)
    }
}