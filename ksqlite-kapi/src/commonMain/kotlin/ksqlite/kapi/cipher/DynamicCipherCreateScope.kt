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
 * Scope available while [DynamicCipher.Factory.create] runs.
 */
public interface DynamicCipherCreateScope {

    /**
     * Returns the current value of the cipher parameter named [name], as configured on the
     * connection the cipher instance is being created for.
     *
     * @throws ksqlite.kapi.SQLiteException if [name] is not a parameter of this cipher.
     */
    public fun getParameter(name: String): Int
}