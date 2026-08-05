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
package ksqlite.internal.test

import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.EEXIST
import platform.posix.errno
import platform.posix.mkdir
import platform.windows.GetTempPathW
import platform.windows.MAX_PATH

public actual fun tempTestDirectory(subdirectory: String): String {
    val tmp = memScoped {
        val buffer = allocArray<UShortVar>(MAX_PATH + 1)
        val len = GetTempPathW(MAX_PATH.toUInt(), buffer)
        require(len > 0u) { "GetTempPathW failed" }
        buffer.toKString()
    }

    val path = "$tmp\\$subdirectory"

    check(mkdir(path) == 0 || errno == EEXIST) {
        "Unable to create temporary directory: $path"
    }

    return path
}