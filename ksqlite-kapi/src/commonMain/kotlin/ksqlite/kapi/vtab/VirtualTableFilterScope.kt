package ksqlite.kapi.vtab

import ksqlite.kapi.value.ProtectedValue

/**
 * Scope to use with [VirtualTableCursor.filter].
 */
public interface VirtualTableFilterScope {

    /**
     * Returns the first value on the right-hand-side of the IN contraint for the [value] parameter.
     */
    public fun inFirst(value: ProtectedValue): ProtectedValue?

    /**
     * Returns the next value on the right-hand-side of the IN contraint for the [value] parameter.
     */
    public fun inNext(value: ProtectedValue): ProtectedValue?
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] for each value on the right-hand-side of the IN contraint for the [value]
 * parameter.
 */
public inline fun VirtualTableFilterScope.inValues(
    value: ProtectedValue,
    block: (ProtectedValue) -> Unit
) {
    var next: ProtectedValue? = inFirst(value) ?: return

    while (next != null) {
        block(next)
        next = inNext(next)
    }
}