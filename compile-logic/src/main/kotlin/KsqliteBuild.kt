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
import komple.platform.Host
import komple.platform.Platform
import org.gradle.api.Project

/**
 * Ksqlite build, allowing custom configuration on CI.
 */
data class KsqliteBuild(
    val enabledPlatforms: List<Platform>,
    val isWasmEnabled: Boolean,
    val isDokka: Boolean
)

/**
 * Returns the build property value for [name] or `null` if is not present.
 */
fun Project.buildProperty(name: String): String? {
    return providers.gradleProperty("ksqlite.build.$name").orNull
}

/**
 * Extracts [KsqliteBuild] from this Gradle project.
 */
fun Project.extractBuild(host: Host): KsqliteBuild {
    val namedPlatforms = Platform.run {
        mapOf(
            "androidArm32" to androidArm32,
            "androidArm64" to androidArm64,
            "androidX64" to androidX64,
            "androidX86" to androidX86,
            "iosArm64" to iosArm64,
            "iosSimulatorArm64" to iosSimulatorArm64,
            "iosX64" to iosX64,
            "linuxArm64" to linuxArm64,
            "linuxX64" to linuxX64,
            "macosArm64" to macosArm64,
            "macosX64" to macosX64,
            "mingwArm64" to mingwArm64,
            "mingwX64" to mingwX64,
            "tvosArm64" to tvosArm64,
            "tvosSimulatorArm64" to tvosSimulatorArm64,
            "tvosX64" to tvosX64,
            "watchosArm32" to watchosArm32,
            "watchosArm64" to watchosArm64,
            "watchosDeviceArm64" to watchosDeviceArm64,
            "watchosSimulatorArm64" to watchosSimulatorArm64,
            "watchosX64" to watchosX64,
        )
    }

    val platforms = buildProperty("platforms")?.split(",")

    val enabledPlatforms = if (platforms != null) {
        namedPlatforms
            .filter { it.key in platforms }
            .values.toList()
    } else {
        namedPlatforms
            .map { it.value }
            .filter { platform ->
                when (host.operatingSystem) {
                    MacOS -> true
                    Linux, Windows -> platform.operatingSystem !is Darwin
                }
            }
    }

    val isWasmEnabled = buildProperty("wasm.enabled")?.toBooleanStrict() ?: true
    val isDokka = buildProperty("dokka")?.toBooleanStrict() ?: false

    return KsqliteBuild(
        enabledPlatforms = enabledPlatforms,
        isWasmEnabled = isWasmEnabled,
        isDokka = isDokka
    )
}