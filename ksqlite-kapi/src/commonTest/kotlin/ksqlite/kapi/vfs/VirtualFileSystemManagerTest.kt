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
package ksqlite.kapi.vfs

import ksqlite.kapi.runSqliteTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests [VirtualFileSystemManager].
 */
class VirtualFileSystemManagerTest {

    @Test
    fun findDefaultVfsWorks() = runSqliteTest { sqlite ->
        val defaultVfs = assertNotNull(sqlite.virtualFileSystems.default)

        assertTrue(defaultVfs.zName.isNotBlank())
        assertTrue(defaultVfs.iVersion.iVersion >= 1)
        assertTrue(defaultVfs.szOsFile > 0)
        assertTrue(defaultVfs.mxPathname > 0)

        // Looking it up by its own name returns the same VFS.
        val vfsByName = assertNotNull(sqlite.virtualFileSystems.find(defaultVfs.zName))
        assertEquals(defaultVfs.zName, vfsByName.zName)
    }

    @Test
    fun findUnknownVfsReturnsNull() = runSqliteTest { sqlite ->
        assertNull(sqlite.virtualFileSystems.find("does-not-exist"))
    }

    @Test
    fun registerAndUnregisterWork() = runSqliteTest { sqlite ->
        val defaultVfs = assertNotNull(sqlite.virtualFileSystems.default)

        // Wrapping the default VFS via the cipher VFS manager is a convenient way to obtain a
        // genuinely standalone, safely disposable VirtualFileSystem to register with, without
        // touching the real default VFS's own registration.
        val wrapped = sqlite.ciphers.virtualFileSystems.create(defaultVfs, makeDefault = false)

        sqlite.virtualFileSystems.register(wrapped, makeDefault = true)

        val newDefault = assertNotNull(sqlite.virtualFileSystems.default)
        assertEquals(wrapped.zName, newDefault.zName)
        assertNotEquals(defaultVfs.zName, newDefault.zName)

        sqlite.virtualFileSystems.unregister(wrapped)
        wrapped.close()
    }

    @Test
    fun unregisteredVfsIsNoLongerFound() = runSqliteTest { sqlite ->
        val defaultVfs = assertNotNull(sqlite.virtualFileSystems.default)
        val wrapped = sqlite.ciphers.virtualFileSystems.create(defaultVfs, makeDefault = false)

        sqlite.virtualFileSystems.register(wrapped, makeDefault = false)
        assertNotNull(sqlite.virtualFileSystems.find(wrapped.zName))

        sqlite.virtualFileSystems.unregister(wrapped)
        assertNull(sqlite.virtualFileSystems.find(wrapped.zName))
        wrapped.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed manager violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceSqliteClosed() = runSqliteTest { sqlite ->
        val manager = sqlite.virtualFileSystems
        val defaultVfs = assertNotNull(manager.default)
        sqlite.close()

        assertFailsWith<IllegalStateException> { manager.default }
        assertFailsWith<IllegalStateException> { manager.register(defaultVfs, false) }
        assertFailsWith<IllegalStateException> { manager.unregister(defaultVfs) }
    }
}
