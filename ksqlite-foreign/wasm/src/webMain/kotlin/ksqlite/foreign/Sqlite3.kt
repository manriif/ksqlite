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
package ksqlite.foreign

import kotlin.js.JsAny

/**
 * SQLite object.
 */
public external interface Sqlite3 : JsAny {

    /**
     * WASM-specific utilities, abstracted to be independent of and configurable for use with,
     * arbitrary WASM runtime environments.
     */
    public val wasm: Sqlite3Wasm
}