rootProject.name = "kotlin-sqlite"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    includeBuild("compile-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()

        ivy {
            url = uri("https://nodejs.org/dist")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }

        ivy {
            url = uri("https://github.com/yarnpkg/yarn/releases/download")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
    }

    versionCatalogs {
        create("kompleLibs") {
            val kompleVersion = file("komple.version").readText()
            from("io.github.manriif.komple:komple-catalog:$kompleVersion")
        }
    }
}

include(":ksqlite-capi")
include(":ksqlite-capi-proxy:client")
include(":ksqlite-capi-proxy:core")
include(":ksqlite-capi-proxy:internal")
include(":ksqlite-capi-proxy:server")
include(":ksqlite-cinterop")
include(":ksqlite-ffm")
include(":ksqlite-jni")
//include(":ksqlite-kapi")
include(":ksqlite-wasm")