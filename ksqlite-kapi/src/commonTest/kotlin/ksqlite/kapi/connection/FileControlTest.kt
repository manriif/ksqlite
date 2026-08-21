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
package ksqlite.kapi.connection

import ksqlite.kapi.runSqliteConnectionTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests [FileControl].
 *
 * Most opcodes are not supported by every VFS, hence the [FileControl] APIs return `null` rather
 * than throwing in that case: only the absence of a thrown exception is asserted for those.
 */
class FileControlTest {

    @Test
    fun optionsWork() = runSqliteConnectionTest { _, connection ->
        val fileControl = connection.fileControl

        val _ = fileControl.systemError
        fileControl.setSizeHint(4_096)
        fileControl.setChunkSize(4_096)

        val _ = fileControl.isPersistWal
        fileControl.isPersistWal = true
        fileControl.isPersistWal = false

        fileControl.setOverwrite(0)

        val _ = fileControl.vfsName

        val _ = fileControl.isPowerSafeOverwrite
        fileControl.isPowerSafeOverwrite = true

        val _ = fileControl.tempFileName

        val _ = fileControl.mmapSize
        fileControl.mmapSize = 0

        val _ = fileControl.hasMoved

        fileControl.setLockTimeout(0)

        val dataVersion = fileControl.dataVersion
        assertTrue(dataVersion >= 0)

        val _ = fileControl.sizeLimit
        fileControl.sizeLimit = -1

        fileControl.reserveBytes(0)

        fileControl.resetCache()
    }

    @Test
    fun operationsFailOnceConnectionClosed() = runSqliteConnectionTest { _, connection ->
        val fileControl = connection.fileControl
        connection.close()

        assertFailsWith<IllegalStateException> { fileControl.systemError }
        assertFailsWith<IllegalStateException> { fileControl.dataVersion }
        assertFailsWith<IllegalStateException> { fileControl.resetCache() }
    }
}
