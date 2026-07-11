plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.conventions.common)
    //alias(libs.plugins.gradle.plugin.publish)
}

val generateKsqliteVersion = tasks.registerKsqlite<GenerateFileTask>("generateKsqliteVersion") {
    outputFile = layout.buildDirectory.file("generated/resources/version.txt")
    content = project.version.toString()
}

tasks.processResources.configure {
    from(generateKsqliteVersion) {
        into(".")
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
}

kotlin {
    configureKotlin()
}

gradlePlugin {
    website = projectWebsite
    vcsUrl = projectGitUrl

    plugins {
        create("ksqlite") {
            id = projectGroup
            implementationClass = "ksqlite.gradle.KsqlitePlugin"
            displayName = "Ksqlite"
            description = localDescription
            //tags = localTags.split(',')
        }
    }
}