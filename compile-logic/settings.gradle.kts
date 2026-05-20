rootProject.name = "compile-logic"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }

        create("kompleLibs") {
            val kompleVersion = file("../komple.version").readText()
            from("io.github.manriif.komple:komple-catalog:$kompleVersion")
        }
    }
}

