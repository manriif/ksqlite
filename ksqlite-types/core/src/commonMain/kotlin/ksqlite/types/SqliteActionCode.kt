@file:Suppress("ClassName")

package ksqlite.types

/**
 * The sqlite3_set_authorizer() interface registers a callback function that is invoked to authorize
 * certain SQL statement actions. The second parameter to the callback is an integer code that
 * specifies what action is being authorized. These are the integer action codes that the authorizer
 * callback may be passed.
 *
 * These action code values signify what kind of operation is to be authorized. The 3rd and 4th
 * parameters to the authorization callback function will be parameters or NULL depending on which
 * of these codes is used as the second parameter. The 5th parameter to the authorizer callback is
 * the name of the database ("main", "temp", etc.) if applicable. The 6th parameter to the
 * authorizer callback is the name of the inner-most trigger or view that is responsible for the
 * access attempt or NULL if this access attempt is directly from top-level SQL code.
 *
 * [Authorizer Action Codes](https://sqlite.org/c3ref/c_alter_table.html).
 */
public sealed class SqliteActionCode(public val code: Int) {

    /**
     * DML code.
     */
    public sealed class Dml(code: Int) : SqliteActionCode(code)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object INDEX : SqliteActionCode(1)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object TABLE : SqliteActionCode(2)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object TEMP_INDEX : SqliteActionCode(3)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object TEMP_TABLE : SqliteActionCode(4)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object TEMP_TRIGGER : SqliteActionCode(5)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object TEMP_VIEW : SqliteActionCode(6)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object TRIGGER : SqliteActionCode(7)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object VIEW : SqliteActionCode(8)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object DELETE : Dml(9)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object DROP_INDEX : SqliteActionCode(10)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object DROP_TABLE : SqliteActionCode(11)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object DROP_TEMP_INDEX : SqliteActionCode(12)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object DROP_TEMP_TABLE : SqliteActionCode(13)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object DROP_TEMP_TRIGGER : SqliteActionCode(14)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object DROP_TEMP_VIEW : SqliteActionCode(15)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object DROP_TRIGGER : SqliteActionCode(16)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object DROP_VIEW : SqliteActionCode(17)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object INSERT : Dml(18)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Pragma  | Name      |1st arg or NULL
     */
    public data object PRAGMA : SqliteActionCode(19)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Column Name      |
     */
    public data object READ : SqliteActionCode(20)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | NULL             | NULL             |
     */
    public data object SELECT : SqliteActionCode(21)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Operation        | NULL             |
     */
    public data object TRANSACTION : SqliteActionCode(22)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Column Name      |
     */
    public data object UPDATE : Dml(23)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Filename         | NULL             |
     */
    public data object ATTACH : SqliteActionCode(24)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Database Name    | NULL             |
     */
    public data object DETACH : SqliteActionCode(25)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Database Name    | Table Name       |
     */
    public data object ALTER_TABLE : SqliteActionCode(26)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | NULL             |
     */
    public data object REINDEX : SqliteActionCode(27)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object ANALYZE : SqliteActionCode(28)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Module Name      |
     */
    public data object VTABLE : SqliteActionCode(29)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Module Name      |
     */
    public data object DROP_VTABLE : SqliteActionCode(30)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | NULL             | Function Name    |
     */
    public data object FUNCTION : SqliteActionCode(31)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Operation        | Savepoint Name   |
     */
    public data object SAVEPOINT : SqliteActionCode(32)

    /**
     * No longer used.
     */
    public data object COPY : SqliteActionCode(0)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | NULL             | NULL             |
     */
    public data object RECURSIVE : SqliteActionCode(33)
}