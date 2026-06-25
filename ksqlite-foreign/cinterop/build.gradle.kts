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
        generateDefFileTaskProvider.configure {
            dependsOn(komple.tools.sqlite.installTaskProvider)
        }

        excludedFunctions = sqliteFunctions(false)
        noStringConversion = KsqliteNoStringConversions

        // TODO remove below option before the library goes stable and direct ccall mode is still an
        //  exprimental feature or if some problems have been encountered
        //
        // Faced during development :
        // - https://youtrack.jetbrains.com/issue/KT-82031
        //
        // Above issues aren't a problem, they're easily recoverable
        extraOpts("-Xccall-mode", "direct")
    }
}