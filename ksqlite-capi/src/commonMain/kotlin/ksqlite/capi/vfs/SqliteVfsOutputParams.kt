package ksqlite.capi.vfs

import ksqlite.capi.memory.Int32TransformOutputParam
import ksqlite.types.SqliteAccessFlag
import ksqlite.types.SqliteOpenFlag

/**
 * Output parameter that accepts an [SqliteAccessFlag].
 */
public class SqliteVfsAccessFlagsOutputParam : Int32TransformOutputParam<SqliteAccessFlag>() {

    override fun transform(value: Int): SqliteAccessFlag = SqliteAccessFlag.from(value)
}

/**
 * Output parameter that accepts an [SqliteOpenFlag.Vfs].
 */
public class SqliteVfsOpenFlagsOutputParam : Int32TransformOutputParam<SqliteOpenFlag.Vfs>() {

    override fun transform(value: Int): SqliteOpenFlag.Vfs = SqliteOpenFlag.Vfs.from(value)
}