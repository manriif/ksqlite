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
package ksqlite.kapi.cipher

/**
 * Prefix of the SQLite Multiple Ciphers VFS.
 */
internal const val SQLITEMC_VFS_PREFIX = "multipleciphers"

private val CipherNameRegex = Regex("^[A-Za-z][A-Za-z0-9_]{0,62}$")

/**
 * Returns the name of a SQLite Multiple Ciphers VFS shim wrapping the VFS named after [realName].
 */
internal fun sqliteMcVfsName(realName: String): String =
    "$SQLITEMC_VFS_PREFIX-$realName"

/**
 * Ensure that [name] matches SQLite Multiple Ciphers name constraints.
 *
 * @throws CipherException if the name is not valid.
 */
internal inline fun ensureValidCipherName(
    name: String,
    lazyMessage: () -> String
) {
    if (!CipherNameRegex.matches(name)) {
        throwCipherException(lazyMessage())
    }
}