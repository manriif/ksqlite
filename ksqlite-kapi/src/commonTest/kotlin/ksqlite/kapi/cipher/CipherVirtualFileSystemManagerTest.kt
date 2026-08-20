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

import ksqlite.kapi.runSqliteTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests [CipherVirtualFileSystemManager].
 */
class CipherVirtualFileSystemManagerTest {

    @Test
    fun createWorks() = runSqliteTest { sqlite ->
        val baseVfs = assertNotNull(sqlite.virtualFileSystems.default)

        val wrapped = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)
        val wrappedName = wrapped.zName
        assertNotEquals(baseVfs.zName, wrappedName)

        assertNotNull(sqlite.virtualFileSystems.find(wrappedName))

        wrapped.close()
        assertNull(sqlite.virtualFileSystems.find(wrappedName))
    }

    @Test
    fun destroyAllWorks() = runSqliteTest { sqlite ->
        val baseVfs = assertNotNull(sqlite.virtualFileSystems.default)

        val first = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)
        val second = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)
        val firstName = first.zName
        val secondName = second.zName

        assertNotNull(sqlite.virtualFileSystems.find(firstName))
        assertNotNull(sqlite.virtualFileSystems.find(secondName))

        sqlite.ciphers.virtualFileSystems.destroyAll()

        assertNull(sqlite.virtualFileSystems.find(firstName))
        assertNull(sqlite.virtualFileSystems.find(secondName))

        // destroyAll() must also invalidate the wrapper objects it previously returned, the same
        // way plain close() does, rather than leaving them pointing at freed native memory.
        assertFailsWith<IllegalStateException> { first.zName }
        assertFailsWith<IllegalStateException> { second.zName }

        // Closing an already-destroyed wrapper is a no-op, and must not attempt to destroy a VFS
        // name that's no longer registered.
        first.close()
        second.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed virtual file system violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runSqliteTest { sqlite ->
        val baseVfs = assertNotNull(sqlite.virtualFileSystems.default)
        val wrapped = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)

        wrapped.close()
        // Closing again is a no-op
        wrapped.close()

        assertFailsWith<IllegalStateException> { wrapped.zName }
        assertFailsWith<IllegalStateException> { wrapped.iVersion }
        assertFailsWith<IllegalStateException> { wrapped.szOsFile }
        assertFailsWith<IllegalStateException> { wrapped.mxPathname }
        assertFailsWith<IllegalStateException> { wrapped.pNext }
    }
}
