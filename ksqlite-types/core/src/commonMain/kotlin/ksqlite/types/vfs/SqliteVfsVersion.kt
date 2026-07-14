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
package ksqlite.types.vfs

/**
 * Virtual file system structure version number.
 */
public enum class SqliteVfsVersion(public val iVersion: Int) {

    /**
     * Initial version.
     */
    VERSION_1(1),

    /**
     * Adds support for xCurrentTimeInt64.
     */
    VERSION_2(2),

    /**
     * Adds support for xSetSystemCall, xGetSystemCall and xNextSystemCall.
     */
    VERSION_3(3)
}