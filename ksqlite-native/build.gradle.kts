import komple.project.c.CLibraryType
import modules.KsqliteNoStringConversions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

kotlin {
    listOf(macosArm64()).forEach { target ->
        target.configureNativeTarget()
    }
}

fun KotlinNativeTarget.configureNativeTarget() {
    komple.projects.kotlinSqlite.createLibrary(CLibraryType.Static, this) {
        excludedFunctions = sqliteFunctions(false)
        noStringConversion = KsqliteNoStringConversions

        extraOpts("-Xccall-mode", "direct")

        generateDefFileTaskProvider.configure {
            dependsOn(komple.tools.sqlite.installTaskProvider)
        }
    }
}