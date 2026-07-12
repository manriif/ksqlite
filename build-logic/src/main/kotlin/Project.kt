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
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import kotlin.properties.ReadOnlyProperty

internal val Project.libs: LibrariesForLibs
    inline get() = the()

///////////////////////////////////////////////////////////////////////////
// Properties
///////////////////////////////////////////////////////////////////////////

private const val ROOT_PROJECT_PROPERTY_PREFIX = "project"
private const val LOCAL_PROJECT_PROPERTY_PREFIX = "local"

private fun Project.getProperty(prefix: String, name: String): String {
    val propertyName = "$prefix.$name"

    if (!hasProperty(propertyName)) {
        error("property $propertyName not found in project `${path}`")
    }

    return property(propertyName).toString()
}

///////////////////////////////////////////////////////////////////////////
// Root project
///////////////////////////////////////////////////////////////////////////

private fun rootProjectProperty(name: String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { thisRef, _ ->
        thisRef.rootProject.getProperty(ROOT_PROJECT_PROPERTY_PREFIX, name)
    }
}

val Project.projectGroup by rootProjectProperty("group")
val Project.projectWebsite by rootProjectProperty("website")
val Project.projectInceptionYear by rootProjectProperty("inceptionYear")
val Project.projectLicenseName by rootProjectProperty("license.name")
val Project.projectLicenseUrl by rootProjectProperty("license.url")
val Project.projectGitBase by rootProjectProperty("git.base")
val Project.projectGitUrl by rootProjectProperty("git.url")

val Project.projectDevId by rootProjectProperty("dev.id")
val Project.projectDevName by rootProjectProperty("dev.name")
val Project.projectDevUrl by rootProjectProperty("dev.url")

///////////////////////////////////////////////////////////////////////////
// Local project
///////////////////////////////////////////////////////////////////////////

private fun localProjectProperty(name: String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { thisRef, _ ->
        thisRef.getProperty(LOCAL_PROJECT_PROPERTY_PREFIX, name)
    }
}

val Project.localName: String by localProjectProperty("name")
val Project.localNamespace: String by localProjectProperty("namespace")
val Project.localDescription: String by localProjectProperty("description")