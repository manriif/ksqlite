plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

kotlin {
    webTargets()

    sourceSets.webMain {
        dependencies {
            api(libs.kotlin.wrappers.js)
        }
    }
}