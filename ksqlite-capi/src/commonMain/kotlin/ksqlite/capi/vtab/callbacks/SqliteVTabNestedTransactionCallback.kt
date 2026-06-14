package ksqlite.capi.vtab.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method provide the virtual table implementation an opportunity to implement nested
 * transactions.
 *
 * When xSavepoint(X,N) is invoked, that is a signal to the virtual table X that it should save its
 * current state as savepoint N. A subsequent call to xRollbackTo(X,R) means that the state of the
 * virtual table should return to what it was when xSavepoint(X,R) was last called. The call to
 * xRollbackTo(X,R) will invalidate all savepoints with N>R; none of the invalided savepoints will
 * be rolled back or released without first being reinitialized by a call to xSavepoint(). A call to
 * xRelease(X,M) invalidates all savepoints where N>=M.
 *
 * [The xBegin Method](https://sqlite.org/vtab.html#the_xsavepoint_xrelease_and_xrollbackto_methods)
 */
public fun interface SqliteVTabNestedTransactionCallback<VTab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xsavepoint_xrelease_and_xrollbackto_methods).
     */
    public fun handle(
        vTab: VTab,
        id: Int
    ): SqliteResultCode.OkOrFailure
}

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

/**
 * xSavepoint callback.
 */
public typealias SqliteVTabSavepointCallback<VTab> = SqliteVTabNestedTransactionCallback<VTab>

/**
 * xRelease callback.
 */
public typealias SqliteVTabReleaseCallback<VTab> = SqliteVTabNestedTransactionCallback<VTab>

/**
 * xRollbackTo callback.
 */
public typealias SqliteVTabRollbackToCallback<VTab> = SqliteVTabNestedTransactionCallback<VTab>