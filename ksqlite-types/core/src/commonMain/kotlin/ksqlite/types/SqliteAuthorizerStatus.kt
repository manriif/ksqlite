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
package ksqlite.types

/**
 * The authorizer callback function must return either SQLITE_OK or one of these two constants in
 * order to signal SQLite whether or not the action is permitted.
 * See the [authorizer documentation](https://sqlite.org/c3ref/set_authorizer.html)
 * for additional information.
 *
 * [Authorizer Return Codes](https://sqlite.org/c3ref/c_deny.html).
 */
public enum class SqliteAuthorizerStatus(public val code: Int) {

    /**
     * Allow the action.
     */
    OK(SqliteResultCode.OK.code),

    /**
     * Abort the SQL statement with an error.
     */
    DENY(1),

    /**
     * Don't allow access, but don't generate an error.
     */
    IGNORE(2)
}