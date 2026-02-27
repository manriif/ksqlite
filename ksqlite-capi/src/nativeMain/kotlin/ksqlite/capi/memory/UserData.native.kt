package ksqlite.capi.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Holder for user data and its destructor.
 */
private class UserData(
    val userData: sqlite3_mutable_pointer,
    val destructor: Sqlite3DestructorCallback,
)

/**
 * Globally referenced [UserData]
 */
private val UserDataMap: MutableMap<COpaquePointer, UserData> by lazy(::hashMapOf)

/**
 * C-static function invoking a [Sqlite3DestructorCallback] against a user data pointer.
 * Removes the stored user data from [UserDataMap]
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val UserDataDestructor = staticCFunction { pointer: COpaquePointer? ->
    checkNotNull(pointer)

    checkNotNull(UserDataMap.remove(pointer)).run {
        destructor(userData)
    }
}

/**
 * Returns [UserDataDestructor] only if [userData] != `null` and [destructor] != `null`.
 */
internal fun userDataDestructor(
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    if (userData == null || destructor == null) {
        return null
    }

    UserDataMap[userData.block.pointer] = UserData(userData, destructor)
    return UserDataDestructor
}