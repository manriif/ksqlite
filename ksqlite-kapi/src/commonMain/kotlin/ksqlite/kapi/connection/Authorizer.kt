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

import ksqlite.types.SqliteActionCode
import ksqlite.types.SqliteAuthorizerStatus

/**
 * Callback to use with [DatabaseConnection.setAuthorizer].
 */
public fun interface Authorizer {

    /**
     * Called once for each [action] SQLite is about to perform while compiling a statement, with
     * up to four action-specific details in [detail1] to [detail4]. Returns whether the action is
     * allowed, denied, or silently ignored.
     */
    public fun apply(
        action: SqliteActionCode,
        detail1: String?,
        detail2: String?,
        detail3: String?,
        detail4: String?
    ): SqliteAuthorizerStatus
}