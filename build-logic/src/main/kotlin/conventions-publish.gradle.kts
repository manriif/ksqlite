/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    `maven-publish`
    com.vanniktech.maven.publish
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = projectGroup,
        artifactId = project.name,
        version = libs.versions.ksqlite.get()
    )

    pom {
        name = localName
        description = localDescription
        url = projectWebsite
        inceptionYear = projectInceptionYear

        licenses {
            license {
                name = projectLicenseName
                url = projectLicenseUrl
            }
        }

        developers {
            developer {
                id = projectDevId
                name = projectDevName
                url = projectDevUrl
            }
        }

        scm {
            url = projectGitUrl
            connection = "scm:git:git://${projectGitBase}.git"
            developerConnection = "scm:git:ssh://git@${projectGitBase}.git"
        }
    }
}