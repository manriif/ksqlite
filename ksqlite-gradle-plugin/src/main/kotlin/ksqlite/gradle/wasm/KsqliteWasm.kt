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
package ksqlite.gradle.wasm

import org.gradle.api.provider.Property

/**
 * Configuration of the WASM application.
 */
public interface KsqliteWasm {

    /**
     * Whether the application intend to use the Origin Private File System as an SQLite Virtual
     * File System.
     */
    //public val enableOpfsVfs: Property<Boolean>

    /**
     * The test runner used by the application.
     * Can be set to null to not generate a configuration for the runner.
     *
     * Default to null.
     */
    public val testRunner: Property<WasmTestRunner>
}