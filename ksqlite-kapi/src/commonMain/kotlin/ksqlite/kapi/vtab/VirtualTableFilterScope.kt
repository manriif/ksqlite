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
package ksqlite.kapi.vtab

import ksqlite.kapi.value.ProtectedValue

/**
 * Scope to use with [VirtualTableCursor.filter].
 */
public interface VirtualTableFilterScope {

    /**
     * Returns the first value on the right-hand-side of the IN() constraint for [value], or
     * `null` if there are none.
     */
    public fun inFirst(value: ProtectedValue): ProtectedValue?

    /**
     * Returns the value following [value] on the right-hand-side of the same IN() constraint,
     * or `null` if there are no more.
     */
    public fun inNext(value: ProtectedValue): ProtectedValue?
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] for each value on the right-hand-side of the IN() constraint for [value].
 */
public inline fun VirtualTableFilterScope.inValues(
    value: ProtectedValue,
    block: (ProtectedValue) -> Unit
) {
    var next: ProtectedValue? = inFirst(value) ?: return

    while (next != null) {
        block(next)
        next = inNext(next)
    }
}