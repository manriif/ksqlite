package ksqlite.structs

/**
 * Structs types as recognized in the JNI side.
 */
internal enum class StructType(val type: Int) {
    IndexInfo(0),
    IndexConstraint(1),
    IndexConstraintUsage(2),
    IndexOrderby(3),
    Module(4),
    Vtab(5),
    VtabCursor(6)
}