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

import ksqlite.internal.runtime.closeable.UnsafeCloseableScope

internal class DynamicCipherParameterRegistryImpl(
    private val callbacks: MutableList<DynamicCipherParameter.() -> Unit>
) : DynamicCipherParameterRegistry,
    UnsafeCloseableScope() {

    /**
     * Registers a new [DynamicCipherParameter] and [configure]s it.
     */
    override fun register(configure: DynamicCipherParameter.() -> Unit): Unit =
        notClosed { callbacks.add(configure) }
}