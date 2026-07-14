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
package modules

import ksqlitePrefixed
import sqlitePrefixed

/**
 * Definition file noStringConversions.
 */
val KsqliteNoStringConversions = listOf(
    "prepare_v2",
    "prepare_v3"
).ksqlitePrefixed() + listOf(
    "bind_pointer",
    "bind_text",
    "bind_text64",
    "blob_open",
    "exec",
    "file_control",
    "filename_database",
    "filename_journal",
    "filename_wal",
    "keyword_check",
    "open",
    "open_v2",
    "prepare_v2",
    "prepare_v3",
    "result_error",
    "result_pointer",
    "result_text",
    "result_text64",
    "serialize",
    "table_column_metadata",
    "uri_parameter",
    "uri_boolean",
    "uri_int64",
    "uri_key",
    "wal_checkpoint_v2"
).sqlitePrefixed()