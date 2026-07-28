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
package ksqlite.foreign.structs

import android.os.Build
import ksqlite.foreign.structLayout
import ksqlite.structs.RawStructType
import ksqlite.structs.StructLayout
import ksqlite.structs.StructLayoutProvider

/**
 * Implementation of [StructLayoutProvider] for JNI.
 */
internal object JniStructLayoutProvider : StructLayoutProvider {

    private val structLayoutCache = mutableMapOf<RawStructType, StructLayout>()

    override fun provide(type: RawStructType): StructLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            structLayoutCache.computeIfAbsent(type) { structLayout(it.value) }
        } else {
            structLayoutCache[type] ?: synchronized(structLayoutCache) {
                structLayoutCache[type] ?: structLayout(type.value).also {
                    structLayoutCache[type] = it
                }
            }
        }
    }
}