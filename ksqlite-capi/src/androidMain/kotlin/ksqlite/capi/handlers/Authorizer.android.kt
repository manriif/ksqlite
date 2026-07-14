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
package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.foreign.callbacks.AuthorizerCallback
import ksqlite.types.internal.convertActionCode

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class AuthorizerHandler<AppData> :
    Handler<SqliteAuthorizerCallback<AppData>, AppData>(),
    AuthorizerCallback {

    override fun apply(
        opId: Int,
        string1: String?,
        string2: String?,
        string3: String?,
        string4: String?
    ): Int = handle { callback, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(opId),
            detail1 = string1,
            detail2 = string2,
            detail3 = string3,
            detail4 = string4
        ).code
    }
}