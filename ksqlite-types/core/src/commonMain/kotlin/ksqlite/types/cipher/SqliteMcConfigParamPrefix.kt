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
package ksqlite.types.cipher

/**
 * Prefixes allowed by the global and cipher configuration functions.
 */
public sealed class SqliteMcConfigParamPrefix(public val value: String) {

    /**
     * Prefixes that can be used for both reading and writing.
     */
    public sealed class ReadWrite(value: String) : SqliteMcConfigParamPrefix(value)

    /**
     * Get or set the transient parameter value. Transient values are only used once for the next
     * call to sqlite3_key() or sqlite3_rekey(). Afterwards, the permanent default values will be
     * used again (see below).
     */
    public data object None : ReadWrite("")

    /**
     * Get or set the permanent default parameter value. Permanent values will be used during the
     * entire lifetime of the db database instance, unless explicitly overridden by a transient
     * value. The initial values for the permanent default values are the compile-time default
     * values.
     */
    public data object Default : ReadWrite("default:")

    /**
     * Get the lower bound of the valid parameter value range. This is read-only.
     */
    public data object Min : SqliteMcConfigParamPrefix("min:")

    /**
     * Get the upper bound of the valid parameter value range. This is read-only.
     */
    public data object Max : SqliteMcConfigParamPrefix("max:")
}