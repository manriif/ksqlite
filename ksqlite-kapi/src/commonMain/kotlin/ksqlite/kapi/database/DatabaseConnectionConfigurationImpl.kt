package ksqlite.kapi.database

import ksqlite.capi.sqlite3_db_config
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingBooleanParam
import ksqlite.kapi.helpers.usingParam

internal class DatabaseConnectionConfigurationImpl(
    private val db: sqlite3,
    private val scope: ClosableScope
) : DatabaseConnectionConfiguration {

    override var isForeignKeyEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_FKEY)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_FKEY)

    override var areTriggersEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_TRIGGER)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_TRIGGER)

    override var isFts3tokenizerEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_FTS3_TOKENIZER)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_FTS3_TOKENIZER)

    override var isLoadExtensionEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_LOAD_EXTENSION)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_LOAD_EXTENSION)

    override var isCheckpointOnCloseDisabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::NO_CKPT_ON_CLOSE)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::NO_CKPT_ON_CLOSE)

    override var isQueryPlannerStabilityGuaranteeEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_QPSG)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_QPSG)

    override var isTriggerExplainQueryPlanEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::TRIGGER_EQP)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::TRIGGER_EQP)

    override var isDefensive: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::DEFENSIVE)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::DEFENSIVE)

    override var isWritableSchema: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::WRITABLE_SCHEMA)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::WRITABLE_SCHEMA)

    override var isLegacyAlterTableBehaviorEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::LEGACY_ALTER_TABLE)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::LEGACY_ALTER_TABLE)

    override var isDoubleQuotedStringDmlEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::DQS_DML)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::DQS_DML)

    override var isDoubleQuotedStringDdlEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::DQS_DDL)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::DQS_DDL)

    override var areViewsEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_VIEW)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_VIEW)

    override var isLegacyFileFormatEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::LEGACY_FILE_FORMAT)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::LEGACY_FILE_FORMAT)

    override var isTrustedSchema: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::TRUSTED_SCHEMA)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::TRUSTED_SCHEMA)

    override var isStatementScanStatusEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::STMT_SCANSTATUS)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::STMT_SCANSTATUS)

    override var isReverseScanOrderEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::REVERSE_SCANORDER)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::REVERSE_SCANORDER)

    override var isAttachCreateEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_ATTACH_CREATE)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_ATTACH_CREATE)

    override var isAttachWriteEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_ATTACH_WRITE)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_ATTACH_WRITE)

    override var areCommentsEnabled: Boolean
        get() = getBooleanOption(SqliteDbConfigOption::ENABLE_COMMENTS)
        set(value) = setBooleanOption(value, SqliteDbConfigOption::ENABLE_COMMENTS)

    override var floatingPointDigits: Int
        get() = getIntOption(0, SqliteDbConfigOption::FP_DIGITS)
        set(value) = setIntOption(value, SqliteDbConfigOption::FP_DIGITS)

    /**
     * Applies the given configuration [option].
     */
    private fun applyOption(option: SqliteDbConfigOption) =
        scope.notClosed { sqliteResultCheck(sqlite3_db_config(db, option)) }

    ///////////////////////////////////////////////////////////////////////////
    // Boolean
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value of the option supplied by [createOption].
     */
    private inline fun getBooleanOption(
        createOption: (Int, Int32OutputParam) -> SqliteDbConfigOption.IntOutput
    ): Boolean = scope.notClosed {
        usingBooleanParam(null) { param ->
            sqliteResultCheck(sqlite3_db_config(db, createOption(-1, param)))
        }
    }

    /**
     * Sets the [value] of the option supplied by [createOption].
     */
    private inline fun setBooleanOption(
        value: Boolean,
        createOption: (Int, Nothing?) -> SqliteDbConfigOption.IntOutput
    ): Unit = applyOption(createOption(if (value) 1 else 0, null))

    ///////////////////////////////////////////////////////////////////////////
    // Int
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value of the option supplied by [createOption].
     */
    private inline fun getIntOption(
        nonAlteringValue: Int,
        createOption: (Int, Int32OutputParam) -> SqliteDbConfigOption.IntOutput
    ): Int = scope.notClosed {
        usingParam(Int32OutputParam(-1)) { param ->
            sqliteResultCheck(sqlite3_db_config(db, createOption(nonAlteringValue, param)))
        }
    }

    /**
     * Sets the [value] of the option supplied by [createOption].
     */
    private inline fun setIntOption(
        value: Int,
        createOption: (Int, Nothing?) -> SqliteDbConfigOption.IntOutput
    ): Unit = applyOption(createOption(value, null))

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    override fun setMainDatabaseName(name: String) =
        applyOption(SqliteDbConfigOption.MAINDBNAME(name))

    override fun setLookasideConfig(buf: Buffer?, sz: Int, cnt: Int) =
        applyOption(SqliteDbConfigOption.LOOKASIDE(buf?.buffer, sz, cnt))

    override fun setResetDatabaseEnabled(enabled: Boolean) =
        applyOption(SqliteDbConfigOption.RESET_DATABASE(if (enabled) 1 else 0))
}