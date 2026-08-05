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

import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.toKString
import platform.posix.EEXIST
import platform.posix.errno
import platform.posix.getenv
import platform.posix.mkdir

@OptIn(UnsafeNumber::class)
public actual fun tempTestDirectory(subdirectory: String): String {
    val tmp = getenv("TMPDIR")?.toKString() ?: "/data/local/tmp"
    val path = "$tmp/$subdirectory"

    check(mkdir(path, 0x1FFu) == 0 || errno == EEXIST) {
        "Unable to create temporary directory: $path"
    }

    return path
}