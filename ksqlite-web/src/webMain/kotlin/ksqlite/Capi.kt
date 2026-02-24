package ksqlite

import kotlin.js.JsAny
import kotlin.js.JsBigInt

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////
/*
// Type aliases for clarity
type sqlite3 = number;
type sqlite3_stmt = number;
type sqlite3_value = number;
type sqlite3_context = number;
type pointer = number;
type cstring = ;

// Auto Extension Functions
/*sqlite3_auto_extension(xEntryPoint: Function): number;
sqlite3_cancel_auto_extension(xEntryPoint: Function): number;
sqlite3_reset_auto_extension(): void;*/

// Binding Functions
sqlite3_bind_blob(stmt: sqlite3_stmt, index: number, blob: Uint8Array | pointer, nBytes: number, destructor: Function | number): number;
sqlite3_bind_double(stmt: sqlite3_stmt, index: number, value: number): number;
sqlite3_bind_int(stmt: sqlite3_stmt, index: number, value: number): number;
sqlite3_bind_int64(stmt: sqlite3_stmt, index: number, value: number | bigint): number;
sqlite3_bind_null(stmt: sqlite3_stmt, index: number): number;
sqlite3_bind_parameter_count(stmt: sqlite3_stmt): number;
sqlite3_bind_parameter_index(stmt: sqlite3_stmt, name: string): number;
sqlite3_bind_parameter_name(stmt: sqlite3_stmt, index: number): string;
sqlite3_bind_pointer(stmt: sqlite3_stmt, index: number, ptr: pointer, type: string, destructor: Function | null): number;
sqlite3_bind_text(stmt: sqlite3_stmt, index: number, text: string | pointer, nBytes: number, destructor: Function | number): number;
sqlite3_clear_bindings(stmt: sqlite3_stmt): number;

// Busy Handler Functions
sqlite3_busy_handler(db: sqlite3, callback: Function | null, pArg: any): number;
sqlite3_busy_timeout(db: sqlite3, ms: number): number;

// Changes Functions
sqlite3_changes(db: sqlite3): number;
sqlite3_changes64(db: sqlite3): number | bigint;
sqlite3_total_changes(db: sqlite3): number;
sqlite3_total_changes64(db: sqlite3): number | bigint;

// Connection Functions
sqlite3_close_v2(db: sqlite3): number;

// Collation Functions
sqlite3_collation_needed(db: sqlite3, pArg: any, callback: Function): number;
sqlite3_create_collation(db: sqlite3, name: string, eTextRep: number, pArg: any, xCompare: Function): number;
sqlite3_create_collation_v2(db: sqlite3, name: string, eTextRep: number, pArg: any, xCompare: Function, xDestroy: Function | null): number;

// Column Access Functions
sqlite3_column_blob(stmt: sqlite3_stmt, iCol: number): Uint8Array;
sqlite3_column_bytes(stmt: sqlite3_stmt, iCol: number): number;
sqlite3_column_count(stmt: sqlite3_stmt): number;
sqlite3_column_database_name(stmt: sqlite3_stmt, iCol: number): string;
sqlite3_column_decltype(stmt: sqlite3_stmt, iCol: number): string;
sqlite3_column_double(stmt: sqlite3_stmt, iCol: number): number;
sqlite3_column_int(stmt: sqlite3_stmt, iCol: number): number;
sqlite3_column_int64(stmt: sqlite3_stmt, iCol: number): number | bigint;
sqlite3_column_js(stmt: sqlite3_stmt, iCol: number): any;
sqlite3_column_name(stmt: sqlite3_stmt, iCol: number): string;
sqlite3_column_origin_name(stmt: sqlite3_stmt, iCol: number): string;
sqlite3_column_table_name(stmt: sqlite3_stmt, iCol: number): string;
sqlite3_column_text(stmt: sqlite3_stmt, iCol: number): string;
sqlite3_column_type(stmt: sqlite3_stmt, iCol: number): number;
sqlite3_column_value(stmt: sqlite3_stmt, iCol: number): sqlite3_value;
sqlite3_data_count(stmt: sqlite3_stmt): number;



// Commit/Rollback Hooks
sqlite3_commit_hook(db: sqlite3, callback: Function | null, pArg: any): any;
sqlite3_rollback_hook(db: sqlite3, callback: Function | null, pArg: any): any;
sqlite3_update_hook(db: sqlite3, callback: Function | null, pArg: any): any;

// Compile Options
sqlite3_compileoption_get(N: number): string | null;
sqlite3_compileoption_used(optName: string): number;

// Complete SQL
sqlite3_complete(sql: string): number;

// Config Functions
sqlite3_config(...args: any[]): number;
sqlite3_db_config(db: sqlite3, op: number, ...args: any[]): number;

// Context Functions
sqlite3_context_db_handle(ctx: sqlite3_context): sqlite3;

// User-Defined Functions
sqlite3_create_function(db: sqlite3, name: string, nArg: number, eTextRep: number, pApp: any, xFunc: Function | null, xStep: Function | null, xFinal: Function | null): number;
sqlite3_create_function_v2(db: sqlite3, name: string, nArg: number, eTextRep: number, pApp: any, xFunc: Function | null, xStep: Function | null, xFinal: Function | null, xDestroy: Function | null): number;
sqlite3_create_window_function(db: sqlite3, name: string, nArg: number, eTextRep: number, pApp: any, xStep: Function | null, xFinal: Function | null, xValue: Function | null, xInverse: Function | null, xDestroy: Function | null): number;

// Virtual Table Module Functions
sqlite3_create_module(db: sqlite3, name: string, pModule: any, pClientData: any): number;
sqlite3_create_module_v2(db: sqlite3, name: string, pModule: any, pClientData: any, xDestroy: Function | null): number;
sqlite3_declare_vtab(db: sqlite3, sql: string): number;
sqlite3_drop_modules(db: sqlite3, azKeep: string[] | null): number;
sqlite3_overload_function(db: sqlite3, name: string, nArg: number): number;

// Database Information
sqlite3_db_filename(db: sqlite3, dbName: string): string;
sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3;
sqlite3_db_name(db: sqlite3, N: number): string;
sqlite3_db_readonly(db: sqlite3, dbName: string): number;
sqlite3_db_status(db: sqlite3, op: number, pCurrent: pointer, pHighwater: pointer, resetFlag: number): number;
sqlite3_db_status64(db: sqlite3, op: number, pCurrent: pointer, pHighwater: pointer, resetFlag: number): number;

// Serialization
sqlite3_deserialize(db: sqlite3, schema: string, pData: pointer, szDb: number | bigint, szBuf: number | bigint, mFlags: number): number;
sqlite3_serialize(db: sqlite3, schema: string, piSize: pointer, mFlags: number): pointer;

// Error Handling
sqlite3_errcode(db: sqlite3): number;
sqlite3_errmsg(db: sqlite3): string;
sqlite3_error_offset(db: sqlite3): number;
sqlite3_errstr(errCode: number): string;
sqlite3_extended_errcode(db: sqlite3): number;
sqlite3_extended_result_codes(db: sqlite3, onoff: number): number;

// Exec
sqlite3_exec(db: sqlite3, sql: string, callback: Function | null, pArg: any, pzErrMsg: pointer): number;

// SQL Expansion
sqlite3_expanded_sql(stmt: sqlite3_stmt): string;
sqlite3_sql(stmt: sqlite3_stmt): string;

// File Control
sqlite3_file_control(db: sqlite3, dbName: string, op: number, pArg: pointer): number;

// Finalize and Reset
sqlite3_finalize(stmt: sqlite3_stmt): number;
sqlite3_reset(stmt: sqlite3_stmt): number;

// Memory Management
sqlite3_free(ptr: pointer): void;
sqlite3_malloc(n: number): pointer;
sqlite3_malloc64(n: number | bigint): pointer;
sqlite3_msize(ptr: pointer): number | bigint;
sqlite3_realloc(ptr: pointer, n: number): pointer;
sqlite3_realloc64(ptr: pointer, n: number | bigint): pointer;

// Autocommit
sqlite3_get_autocommit(db: sqlite3): number;

// Auxiliary Data
sqlite3_get_auxdata(ctx: sqlite3_context, N: number): any;
sqlite3_set_auxdata(ctx: sqlite3_context, N: number, pAux: any, xDelete: Function | null): void;

// Initialize/Shutdown
sqlite3_initialize(): number;
sqlite3_shutdown(): number;

// Interrupt
sqlite3_interrupt(db: sqlite3): void;
sqlite3_is_interrupted(db: sqlite3): number;

// JavaScript-Specific Functions
sqlite3_js_aggregate_context(ctx: sqlite3_context, n: number): any;
sqlite3_js_db_export(db: sqlite3): Uint8Array;
sqlite3_js_db_uses_vfs(db: sqlite3, vfsName: string): boolean;
sqlite3_js_db_vfs(db: sqlite3, asObject: boolean): any;
sqlite3_js_kvvfs_clear(dbName?: string): void;
sqlite3_js_kvvfs_size(dbName?: string): number;
sqlite3_js_posix_create_file(filename: string, data: string | Uint8Array, dataLen?: number): void;
sqlite3_js_rc_str(resultCode: number): string;
sqlite3_js_sql_to_string(sql: string | pointer): string;
sqlite3_js_vfs_create_file(vfsName: string, filename: string, data: string | Uint8Array, dataLen?: number): void;
sqlite3_js_vfs_list(): string[];

// Keywords
sqlite3_keyword_check(word: string): number;
sqlite3_keyword_count(): number;
sqlite3_keyword_name(N: number): string;

// Last Insert Rowid
sqlite3_last_insert_rowid(db: sqlite3): number | bigint;
sqlite3_set_last_insert_rowid(db: sqlite3, rowid: number | bigint): void;

// Library Version
sqlite3_libversion(): string;
sqlite3_libversion_number(): number;
sqlite3_sourceid(): string;

// Limits
sqlite3_limit(db: sqlite3, id: number, newVal: number): number;

// Next Statement
sqlite3_next_stmt(db: sqlite3, pStmt: sqlite3_stmt | null): sqlite3_stmt;

// Open Database
sqlite3_open(filename: string, ppDb: pointer): number;
sqlite3_open_v2(filename: string, ppDb: pointer, flags: number, vfsName: string | null): number;

// Prepare Statement
sqlite3_prepare_v2(db: sqlite3, sql: string | pointer, nByte: number, ppStmt: pointer, pzTail: pointer): number;
sqlite3_prepare_v3(db: sqlite3, sql: string | pointer, nByte: number, prepFlags: number, ppStmt: pointer, pzTail: pointer): number;

// Pre-Update Hook
sqlite3_preupdate_blobwrite(db: sqlite3): number;
sqlite3_preupdate_count(db: sqlite3): number;
sqlite3_preupdate_depth(db: sqlite3): number;
sqlite3_preupdate_hook(db: sqlite3, xPreUpdate: Function | null, pArg: any): any;
sqlite3_preupdate_new(db: sqlite3, iCol: number): sqlite3_value;
sqlite3_preupdate_new_js(db: sqlite3, iCol: number): any;
sqlite3_preupdate_old(db: sqlite3, iCol: number): sqlite3_value;
sqlite3_preupdate_old_js(db: sqlite3, iCol: number): any;

// Progress Handler
sqlite3_progress_handler(db: sqlite3, nOps: number, xProgress: Function | null, pArg: any): void;

// Randomness
sqlite3_randomness(N: number, P: pointer): void;

// Result Functions (for UDFs)
sqlite3_result_blob(ctx: sqlite3_context, blob: Uint8Array | pointer, n: number, destructor: Function | number): void;
sqlite3_result_double(ctx: sqlite3_context, value: number): void;
sqlite3_result_error(ctx: sqlite3_context, msg: string, n: number): void;
sqlite3_result_error_code(ctx: sqlite3_context, errCode: number): void;
sqlite3_result_error_js(ctx: sqlite3_context, value: any): void;
sqlite3_result_error_nomem(ctx: sqlite3_context): void;
sqlite3_result_error_toobig(ctx: sqlite3_context): void;
sqlite3_result_int(ctx: sqlite3_context, value: number): void;
sqlite3_result_int64(ctx: sqlite3_context, value: number | bigint): void;
sqlite3_result_js(ctx: sqlite3_context, value: any): void;
sqlite3_result_null(ctx: sqlite3_context): void;
sqlite3_result_pointer(ctx: sqlite3_context, ptr: pointer, type: string, destructor: Function | null): void;
sqlite3_result_subtype(ctx: sqlite3_context, subtype: number): void;
sqlite3_result_text(ctx: sqlite3_context, text: string | pointer, n: number, destructor: Function | number): void;
sqlite3_result_zeroblob(ctx: sqlite3_context, n: number): number;
sqlite3_result_zeroblob64(ctx: sqlite3_context, n: number | bigint): number;

// Authorizer
sqlite3_set_authorizer(db: sqlite3, xAuth: Function | null, pUserData: any): number;

// Error Message
sqlite3_set_errmsg(ctx: sqlite3_context, msg: string): void;

// Status
sqlite3_status(op: number, pCurrent: pointer, pHighwater: pointer, resetFlag: number): number;
sqlite3_status64(op: number, pCurrent: pointer, pHighwater: pointer, resetFlag: number): number;

// Step
sqlite3_step(stmt: sqlite3_stmt): number;

// Statement Status
sqlite3_stmt_busy(stmt: sqlite3_stmt): number;
sqlite3_stmt_explain(stmt: sqlite3_stmt, eMode: number): number;
sqlite3_stmt_isexplain(stmt: sqlite3_stmt): number;
sqlite3_stmt_readonly(stmt: sqlite3_stmt): number;
sqlite3_stmt_status(stmt: sqlite3_stmt, op: number, resetFlg: number): number;

// String Functions
sqlite3_strglob(pattern: string, str: string): number;
sqlite3_stricmp(str1: string, str2: string): number;
sqlite3_strlike(pattern: string, str: string, escapeChar: number): number;
sqlite3_strnicmp(str1: string, str2: string, n: number): number;

// Table Column Metadata
sqlite3_table_column_metadata(db: sqlite3, dbName: string, tableName: string, columnName: string, pzDataType: pointer, pzCollSeq: pointer, pNotNull: pointer, pPrimaryKey: pointer, pAutoinc: pointer): number;

// Trace
sqlite3_trace_v2(db: sqlite3, uMask: number, xCallback: Function, pCtx: any): number;

// Transaction State
sqlite3_txn_state(db: sqlite3, schema: string | null): number;

// URI Functions
sqlite3_uri_boolean(filename: string, param: string, defaultValue: number): number;
sqlite3_uri_int64(filename: string, param: string, defaultValue: number | bigint): number | bigint;
sqlite3_uri_key(filename: string, N: number): string;
sqlite3_uri_parameter(filename: string, param: string): string;

// User Data
sqlite3_user_data(ctx: sqlite3_context): any;

// Value Functions
sqlite3_value_blob(pVal: sqlite3_value): Uint8Array;
sqlite3_value_bytes(pVal: sqlite3_value): number;
sqlite3_value_double(pVal: sqlite3_value): number;
sqlite3_value_dup(pVal: sqlite3_value): sqlite3_value;
sqlite3_value_free(pVal: sqlite3_value): void;
sqlite3_value_frombind(pVal: sqlite3_value): number;
sqlite3_value_int(pVal: sqlite3_value): number;
sqlite3_value_int64(pVal: sqlite3_value): number | bigint;
sqlite3_value_nochange(pVal: sqlite3_value): number;
sqlite3_value_numeric_type(pVal: sqlite3_value): number;
sqlite3_value_pointer(pVal: sqlite3_value, type: string): pointer;
sqlite3_value_subtype(pVal: sqlite3_value): number;
sqlite3_value_text(pVal: sqlite3_value): string;
sqlite3_value_to_js(pVal: sqlite3_value): any;
sqlite3_value_type(pVal: sqlite3_value): number;
sqlite3_values_to_js(argc: number, argv: pointer): any[];

// VFS Functions
sqlite3_vfs_find(name: string | null): pointer;
sqlite3_vfs_register(pVfs: pointer, makeDflt: number): number;
sqlite3_vfs_unregister(pVfs: pointer): number;

// Virtual Table Functions
sqlite3_vtab_collation(pIdxInfo: pointer, iCons: number): string;
sqlite3_vtab_config(db: sqlite3, op: number, ...args: any[]): number;
sqlite3_vtab_distinct(pIdxInfo: pointer): number;
sqlite3_vtab_in(pIdxInfo: pointer, iCons: number, bHandle: number): number;
sqlite3_vtab_in_first(pVal: sqlite3_value, ppOut: pointer): number;
sqlite3_vtab_in_next(pVal: sqlite3_value, ppOut: pointer): number;
sqlite3_vtab_nochange(ctx: sqlite3_context): number;
sqlite3_vtab_on_conflict(db: sqlite3): number;
sqlite3_vtab_rhs_value(pIdxInfo: pointer, iCons: number, ppVal: pointer): number;
*/
/**
 * SQLite C-API exposed functions.
 */
@Suppress("FunctionName", "SpellCheckingInspection")
public external interface Capi : JsAny {

    public fun sqlite3_aggregate_context(ctx: JsBigInt, nBytes: Int): JsBigInt

    public fun sqlite3_auto_extension(xEntryPoint: JsAny)

    public fun sqlite3_bind_blob()

    public fun sqlite3_bind_double()

    public fun sqlite3_bind_int()

    public fun sqlite3_bind_int64()

    public fun sqlite3_bind_null()

    public fun sqlite3_bind_parameter_count()

    public fun sqlite3_bind_parameter_index()

    public fun sqlite3_bind_parameter_name()

    public fun sqlite3_bind_pointer()

    public fun sqlite3_bind_text()

    public fun sqlite3_busy_handler()

    public fun sqlite3_busy_timeout()

    public fun sqlite3_cancel_auto_extension()

    public fun sqlite3_changes()

    public fun sqlite3_changes64()

    public fun sqlite3_clear_bindings()

    public fun sqlite3_close_v2()

    public fun sqlite3_collation_needed()

    public fun sqlite3_column_blob()

    public fun sqlite3_column_bytes()

    public fun sqlite3_column_count()

    public fun sqlite3_column_database_name()

    public fun sqlite3_column_decltype()

    public fun sqlite3_column_double()

    public fun sqlite3_column_int()

    public fun sqlite3_column_int64()

    public fun sqlite3_column_js()

    public fun sqlite3_column_name()

    public fun sqlite3_column_origin_name()

    public fun sqlite3_column_table_name()

    public fun sqlite3_column_text()

    public fun sqlite3_column_type()

    public fun sqlite3_column_value()

    public fun sqlite3_commit_hook()

    public fun sqlite3_compileoption_get()

    public fun sqlite3_compileoption_used()

    public fun sqlite3_complete()

    public fun sqlite3_config()

    public fun sqlite3_context_db_handle()

    public fun sqlite3_create_collation()

    public fun sqlite3_create_collation_v2()

    public fun sqlite3_create_function()

    public fun sqlite3_create_function_v2()

    public fun sqlite3_create_module()

    public fun sqlite3_create_module_v2()

    public fun sqlite3_create_window_function()

    public fun sqlite3_data_count()

    public fun sqlite3_db_config()

    public fun sqlite3_db_filename()

    public fun sqlite3_db_handle()

    public fun sqlite3_db_name()

    public fun sqlite3_db_readonly()

    public fun sqlite3_db_status()

    public fun sqlite3_db_status64()

    public fun sqlite3_declare_vtab()

    public fun sqlite3_deserialize()

    public fun sqlite3_drop_modules()

    public fun sqlite3_errcode()

    public fun sqlite3_errmsg()

    public fun sqlite3_error_offset()

    public fun sqlite3_errstr()

    public fun sqlite3_exec()

    public fun sqlite3_expanded_sql()

    public fun sqlite3_extended_errcode()

    public fun sqlite3_extended_result_codes()

    public fun sqlite3_file_control()

    public fun sqlite3_finalize()

    public fun sqlite3_free()

    public fun sqlite3_get_autocommit()

    public fun sqlite3_get_auxdata()

    public fun sqlite3_initialize()

    public fun sqlite3_interrupt()

    public fun sqlite3_is_interrupted()

    public fun sqlite3_js_aggregate_context()

    public fun sqlite3_js_db_export()

    public fun sqlite3_js_db_uses_vfs()

    public fun sqlite3_js_db_vfs()

    public fun sqlite3_js_kvvfs_clear()

    public fun sqlite3_js_kvvfs_size()

    public fun sqlite3_js_posix_create_file()

    public fun sqlite3_js_rc_str()

    public fun sqlite3_js_sql_to_string()

    public fun sqlite3_js_vfs_create_file()

    public fun sqlite3_js_vfs_list()

    public fun sqlite3_keyword_check()

    public fun sqlite3_keyword_count()

    public fun sqlite3_keyword_name()

    public fun sqlite3_last_insert_rowid()

    public fun sqlite3_libversion(): String

    public fun sqlite3_libversion_number()

    public fun sqlite3_limit()

    public fun sqlite3_malloc()

    public fun sqlite3_malloc64()

    public fun sqlite3_msize()

    public fun sqlite3_next_stmt()

    public fun sqlite3_open()

    public fun sqlite3_open_v2()

    public fun sqlite3_overload_function()

    public fun sqlite3_prepare_v2()

    public fun sqlite3_prepare_v3()

    public fun sqlite3_preupdate_blobwrite()

    public fun sqlite3_preupdate_count()

    public fun sqlite3_preupdate_depth()

    public fun sqlite3_preupdate_hook()

    public fun sqlite3_preupdate_new()

    public fun sqlite3_preupdate_new_js()

    public fun sqlite3_preupdate_old()

    public fun sqlite3_preupdate_old_js()

    public fun sqlite3_progress_handler()

    public fun sqlite3_randomness()

    public fun sqlite3_realloc()

    public fun sqlite3_realloc64()

    public fun sqlite3_reset()

    public fun sqlite3_reset_auto_extension()

    public fun sqlite3_result_blob()

    public fun sqlite3_result_double()

    public fun sqlite3_result_error()

    public fun sqlite3_result_error_code()

    public fun sqlite3_result_error_js()

    public fun sqlite3_result_error_nomem()

    public fun sqlite3_result_error_toobig()

    public fun sqlite3_result_int()

    public fun sqlite3_result_int64()

    public fun sqlite3_result_js()

    public fun sqlite3_result_null()

    public fun sqlite3_result_pointer()

    public fun sqlite3_result_subtype()

    public fun sqlite3_result_text()

    public fun sqlite3_result_zeroblob()

    public fun sqlite3_result_zeroblob64()

    public fun sqlite3_rollback_hook()

    public fun sqlite3_serialize()

    public fun sqlite3_set_authorizer()

    public fun sqlite3_set_auxdata()

    public fun sqlite3_set_errmsg()

    public fun sqlite3_set_last_insert_rowid()

    public fun sqlite3_shutdown()

    public fun sqlite3_sourceid()

    public fun sqlite3_sql()

    public fun sqlite3_status()

    public fun sqlite3_status64()

    public fun sqlite3_step()

    public fun sqlite3_stmt_busy()

    public fun sqlite3_stmt_explain()

    public fun sqlite3_stmt_isexplain()

    public fun sqlite3_stmt_readonly()

    public fun sqlite3_stmt_status()

    public fun sqlite3_strglob()

    public fun sqlite3_stricmp()

    public fun sqlite3_strlike()

    public fun sqlite3_strnicmp()

    public fun sqlite3_table_column_metadata()

    public fun sqlite3_total_changes()

    public fun sqlite3_total_changes64()

    public fun sqlite3_trace_v2()

    public fun sqlite3_txn_state()

    public fun sqlite3_update_hook()

    public fun sqlite3_uri_boolean()

    public fun sqlite3_uri_int64()

    public fun sqlite3_uri_key()

    public fun sqlite3_uri_parameter()

    public fun sqlite3_user_data()

    public fun sqlite3_value_blob()

    public fun sqlite3_value_bytes()

    public fun sqlite3_value_double()

    public fun sqlite3_value_dup()

    public fun sqlite3_value_free()

    public fun sqlite3_value_frombind()

    public fun sqlite3_value_int()

    public fun sqlite3_value_int64()

    public fun sqlite3_value_nochange()

    public fun sqlite3_value_numeric_type()

    public fun sqlite3_value_pointer()

    public fun sqlite3_value_subtype()

    public fun sqlite3_value_text()

    public fun sqlite3_value_to_js()

    public fun sqlite3_value_type()

    public fun sqlite3_values_to_js()

    public fun sqlite3_vfs_find()

    public fun sqlite3_vfs_register()

    public fun sqlite3_vfs_unregister()

    public fun sqlite3_vtab_collation()

    public fun sqlite3_vtab_config()

    public fun sqlite3_vtab_distinct()

    public fun sqlite3_vtab_in()

    public fun sqlite3_vtab_in_first()

    public fun sqlite3_vtab_in_next()

    public fun sqlite3_vtab_nochange()

    public fun sqlite3_vtab_on_conflict()

    public fun sqlite3_vtab_rhs_value()
}