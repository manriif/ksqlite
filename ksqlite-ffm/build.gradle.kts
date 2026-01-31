import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import tasks.TASK_JEXTRACT_EXTRACT

plugins {
    alias(libs.plugins.conventions.kmp)
}

val

registerTaskForIde(rootProject.tasks.named(TASK_JEXTRACT_EXTRACT))

kotlin {
    jvmTargets(libs.versions.jvm.target.ffm).forEach { target ->
        target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {

        }
    }
}