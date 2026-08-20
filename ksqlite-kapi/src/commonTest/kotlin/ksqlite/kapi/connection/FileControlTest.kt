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

        val _ = fileControl.getSystemError()
        fileControl.setSizeHint(4_096)
        fileControl.setChunkSize(4_096)

        val _ = fileControl.isPersistWal()
        fileControl.setPersistWal(true)
        fileControl.setPersistWal(false)

        fileControl.setOverwrite(0, null)

        val _ = fileControl.getVfsName()

        val _ = fileControl.isPowerSafeOverwrite()
        fileControl.setPowerSafeOverwrite(true)

        val _ = fileControl.getTempFileName()

        val _ = fileControl.getMmapSize()
        fileControl.setMmapSize(0)

        val _ = fileControl.hasMoved()

        fileControl.setLockTimeout(0)

        val dataVersion = fileControl.getDataVersion()
        assertTrue(dataVersion >= 0)

        val _ = fileControl.getSizeLimit()
        fileControl.setSizeLimit(-1)

        fileControl.reserveBytes(0, null)

        fileControl.resetCache()
    }

    @Test
    fun operationsFailOnceConnectionClosed() = runSqliteConnectionTest { _, connection ->
        val fileControl = connection.fileControl
        connection.close()

        assertFailsWith<IllegalStateException> { fileControl.getSystemError() }
        assertFailsWith<IllegalStateException> { fileControl.getDataVersion() }
        assertFailsWith<IllegalStateException> { fileControl.resetCache() }
    }
}
