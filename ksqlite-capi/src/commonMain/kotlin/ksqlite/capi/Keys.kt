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

///////////////////////////////////////////////////////////////////////////
// Keys used to store buffers that are not copied by SQLite and are then managed by the application
///////////////////////////////////////////////////////////////////////////

internal const val KEY_DB_CONFIG_MAINDBNAME = "db_config_maindbname"

///////////////////////////////////////////////////////////////////////////
// Keys used to replace callbacks in memory manager
///////////////////////////////////////////////////////////////////////////

internal const val KEY_BUSY_HANDLER = "busy_handler"
internal const val KEY_COLLATION_NEEDED = "collation_needed"
internal const val KEY_COMMIT_HOOK = "commit_hook"
internal const val KEY_CONFIG_LOG = "config_log"
internal const val KEY_CONFIG_SQLLOG = "config_sqllog"
internal const val KEY_PREUPDATE_HOOK = "preupdate_hook"
internal const val KEY_PROGRESS_HANDLER = "progress_handler"
internal const val KEY_ROLLBACK_HOOK = "rollback_hook"
internal const val KEY_SET_AUTHORIZER = "set_authorizer"
internal const val KEY_TRACE = "trace"
internal const val KEY_UPDATE_HOOK = "update_hook"
internal const val KEY_WAL_HOOK = "wal_hook"