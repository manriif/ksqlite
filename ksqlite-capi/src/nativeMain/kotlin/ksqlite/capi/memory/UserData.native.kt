package ksqlite.capi.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.Sqlite3DestructorCallback as Destructor

/**
 * Holder for user data and its
 */
private class UserData(
    val pointer: sqlite3_mutable_pointer,
    val destructorCallback: Destructor,
)

/**
 * Globally referenced [UserData]
 */
private val GlobalUserData: MutableMap<COpaquePointer, UserData> by lazy(::mutableMapOf)

/**
 * C-static function invoking a [Destructor] for a user data pointer.
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val UserDataDestructor = staticCFunction { pointer: COpaquePointer? ->
    checkNotNull(pointer)
    checkNotNull(UserDataDestructors[pointer]).invoke()
    check(Disposables[pointer] == null)
}

/**
 * Returns [GlobalDisposer] only if [data] != `null`.
 */
internal fun globalDisposer(data: Any?): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return GlobalDisposer.takeIf { data != null }
}

/**
 * Registers [disposable] pointed by [pointer].
 * [disposable] is expected to be disposed later and a [globalDisposer] should be requested to
 * dispose it.
 */
internal fun registerGlobalDisposable(pointer: COpaquePointer, disposable: Disposable) {
    check(Disposables.put(pointer, disposable) == null) {
        "A disposable is already registered for the pointed address"
    }
}

