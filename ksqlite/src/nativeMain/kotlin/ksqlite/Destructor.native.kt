package ksqlite

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.staticCFunction
import sqlite.sqlite3_destructor_type

/**
 * Returns a [CPointer] for `this` [Destructor].
 */
internal fun <T : Any> Destructor<T>.toCPointer(pinned: Pinned<T>): sqlite3_destructor_type? {
    return when (this) {
        is DestructorFunction -> staticCFunction<COpaquePointer?, Unit> {

        }

        SpecialDestructor.Static -> sqlite.SQLITE_STATIC
        SpecialDestructor.Transient -> sqlite.SQLITE_TRANSIENT
    }
}

internal fun destructor() {

}