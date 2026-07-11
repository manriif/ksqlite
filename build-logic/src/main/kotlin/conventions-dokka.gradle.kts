plugins {
    org.jetbrains.dokka
}

dokka {
    dokkaPublications.html {
        moduleName = localName
        includes.from("README.md")
    }

    pluginsConfiguration.html {
        footerMessage = "© $projectInceptionYear $projectDevName"
    }
}