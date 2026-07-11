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