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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Name of the ksqlite extension.
 */
const val KSQLITE_EXTENSION_NAME = "ksqlite"

/**
 * Extension for [KsqlitePlugin].
 */
interface KsqliteExtension {

    /**
     * Directory where the additional ksqlite C related files are located.
     */
    val ksqliteDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is located.
     */
    val sqliteDirectory: DirectoryProperty

    /**
     * Name of the generated library.
     */
    val libraryName: Property<String>
}