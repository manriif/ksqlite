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
import komple.gradle.task.track
import komple.task.TaskStateTracker
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

/**
 * Name of the Kotlin SQLite library
 * It is the name of the library and ksqlite function prefix.
 */
const val KSQLITE = "ksqlite"

///////////////////////////////////////////////////////////////////////////
// Naming
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a new list with all items prefixed with [SQLITE3] and [joint].
 */
fun Iterable<String>.ksqlitePrefixed(joint: Char = '_'): List<String> {
    return map { "${KSQLITE}${joint}${it}" }
}

///////////////////////////////////////////////////////////////////////////
// Typedefs
///////////////////////////////////////////////////////////////////////////

/**
 * List of typedefs exposed by `ksqlite.h`.
 */
val KsqliteTypedefs = listOf(
    "xLog",
    "xSqllog",
    "xEntryPoint",
    "cipher_descriptor",
    "cipher_params"
).ksqlitePrefixed()

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * List of Ksqlite functions extending SQLite ones
 */
val KsqliteFunctions = listOf(
    "auto_extension",
    "cancel_auto_extension",
    "prepare_v2",
    "prepare_v3",
    "struct_layout_allocate",
    "struct_layout_free"
).ksqlitePrefixed()

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////

const val KSQLITE_TASK_GROUP = "ksqlite"

/**
 * Registers a Ksqlite task.
 */
inline fun <reified T : Task> TaskContainer.registerKsqlite(
    name: String,
    noinline action: T.() -> Unit
) = register<T>(name) {
    group = KSQLITE_TASK_GROUP
    action()
}

/**
 * Registers a Ksqlite task.
 */
inline fun <reified T : Task> TaskContainer.registerKsqliteTracked(
    name: String,
    noinline action: T.(tracker: TaskStateTracker) -> Unit
) = register<T>(name) {
    group = KSQLITE_TASK_GROUP
    track(action)
}