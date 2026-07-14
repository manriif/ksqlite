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
package ksqlite.capi

import ksqlite.capi.memory.StaticMemoryAllocator
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setPointerValue
import ksqlite.foreign.ksqlite_xEntryPoint
import java.lang.foreign.MemorySegment

/**
 * Singleton handler for auto extensions.
 */
internal val AutoExtensionHandler = ksqlite_xEntryPoint.allocate({ db, pzErrMsg, pThunk ->
    autoExtensionHandle(
        db = sqlite3(db),
        api = pThunk,
        errorPointer = pzErrMsg.takeUnless(MemorySegment::isNull)
    ) { errorPointer, message ->
        errorPointer.setPointerValue(sqlite3_mprintf(message))
    }
}, StaticMemoryAllocator)