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

import ksqlite.kapi.result.ResultScope

/**
 * Scope to use with [VirtualTableCursor.column].
 */
public interface VirtualTableColumnScope : ResultScope {

    /**
     * Returns `true` if and only if the call is during an UPDATE operation and the value of the
     * column will not be modified by the UPDATE.
     */
    public val nochange: Boolean
}