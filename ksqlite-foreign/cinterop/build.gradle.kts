import komple.project.c.CLibraryType
import modules.KsqliteNoStringConversions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

kotlin {
    nativeTargets().forEach { target ->
        target.configureNativeTarget()
    }
}

fun KotlinNativeTarget.configureNativeTarget() {
    komple.projects.kotlinSqlite.createLibrary(CLibraryType.Static, this) {
        excludedFunctions = sqliteFunctions(false)
        noStringConversion = KsqliteNoStringConversions

        // TODO remove this before the library goes stable and this is still an exprimental feature
        //  or if some problems have been encountered
        extraOpts("-Xccall-mode", "direct")

        generateDefFileTaskProvider.configure {
            dependsOn(komple.tools.sqlite.installTaskProvider)
        }
    }
}