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
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.internal.test

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.random.Random

/**
 * Indicates whether the platform use SQLite WASM.
 */
public expect val isWasm: Boolean

/**
 * Returns the path to [subdirectory] against the OS temporary directory.
 * The returned directory is created if it does not exist and must be writable.
 */
public expect fun tempTestDirectory(subdirectory: String): String

/**
 * Returns the path to the temporary test directory.
 */
public fun ksqliteTempTestDirectory(): String = tempTestDirectory("ksqlite-test")

/**
 * Random per this process, so a path this returns cannot collide with one from another process.
 */
private val processId = Random.nextInt()

/**
 * Counts calls to [ksqliteTempTestFile] within this process, so two calls never collide even with
 * the same [fileName].
 */
private val callCount = AtomicInt(0)

/**
 * Returns the path to a temporary test file named after [fileName].
 * The returned path is prefixed in a way it is unique to the process.
 */
public fun ksqliteTempTestFile(fileName: String): String {
    val unique = "$processId-${callCount.incrementAndFetch()}"
    val uniqueFileName = "$unique-$fileName"
    return "${ksqliteTempTestDirectory()}/$uniqueFileName"
}