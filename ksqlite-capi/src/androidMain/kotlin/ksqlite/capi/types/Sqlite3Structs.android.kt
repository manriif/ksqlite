@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.GenericPointer
import org.sqlite.jni.capi.NativePointerHolder

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3 = org.sqlite.jni.capi.sqlite3
internal typealias s3_backup = org.sqlite.jni.capi.sqlite3_backup
internal typealias s3_blob = org.sqlite.jni.capi.sqlite3_blob
internal typealias s3_context = org.sqlite.jni.capi.sqlite3_context
internal typealias s3_stmt = org.sqlite.jni.capi.sqlite3_stmt
internal typealias s3_value = org.sqlite.jni.capi.sqlite3_value

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public abstract class HolderGenericPointer<Holder : NativePointerHolder<Holder>>(
    internal val holder: Holder
) : GenericPointer(holder.nativePointer)

public actual class sqlite3 internal constructor(holder: s3) :
    HolderGenericPointer<s3>(holder)

public actual class sqlite3_backup internal constructor(holder: s3_backup) :
    HolderGenericPointer<s3_backup>(holder)

public actual class sqlite3_blob internal constructor(holder: s3_blob) :
    HolderGenericPointer<s3_blob>(holder)

public actual class sqlite3_api_routines internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_context internal constructor(holder: s3_context) :
    HolderGenericPointer<s3_context>(holder)

public actual class sqlite3_index_info internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_module internal constructor(pointer: Long) :
    GenericPointer(pointer)

/*public actual class sqlite3_snapshot internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)*/

public actual class sqlite3_stmt internal constructor(holder: s3_stmt) :
    HolderGenericPointer<s3_stmt>(holder)

public actual class sqlite3_value internal constructor(holder: s3_value) :
    HolderGenericPointer<s3_value>(holder)

public actual class sqlite3_vfs internal constructor(pointer: Long) :
    GenericPointer(pointer)
