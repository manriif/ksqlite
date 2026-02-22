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
public sealed class Sqlite3ActionCode(internal val code: Int) {

    /**
     * DML code.
     */
    public sealed class Dml(code: Int) : Sqlite3ActionCode(code)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object INDEX : Sqlite3ActionCode(1)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object TABLE : Sqlite3ActionCode(2)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object TEMP_INDEX : Sqlite3ActionCode(3)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object TEMP_TABLE : Sqlite3ActionCode(4)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object TEMP_TRIGGER : Sqlite3ActionCode(5)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object TEMP_VIEW : Sqlite3ActionCode(6)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object TRIGGER : Sqlite3ActionCode(7)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object VIEW : Sqlite3ActionCode(8)

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
    public data object DROP_INDEX : Sqlite3ActionCode(10)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object DROP_TABLE : Sqlite3ActionCode(11)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object DROP_TEMP_INDEX : Sqlite3ActionCode(12)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object DROP_TEMP_TABLE : Sqlite3ActionCode(13)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object DROP_TEMP_TRIGGER : Sqlite3ActionCode(14)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object DROP_TEMP_VIEW : Sqlite3ActionCode(15)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object DROP_TRIGGER : Sqlite3ActionCode(16)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object DROP_VIEW : Sqlite3ActionCode(17)

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
    public data object PRAGMA : Sqlite3ActionCode(19)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Column Name      |
     */
    public data object READ : Sqlite3ActionCode(20)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | NULL             | NULL             |
     */
    public data object SELECT : Sqlite3ActionCode(21)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Operation        | NULL             |
     */
    public data object TRANSACTION : Sqlite3ActionCode(22)

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
    public data object ATTACH : Sqlite3ActionCode(24)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Database Name    | NULL             |
     */
    public data object DETACH : Sqlite3ActionCode(25)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Database Name    | Table Name       |
     */
    public data object ALTER_TABLE : Sqlite3ActionCode(26)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | NULL             |
     */
    public data object REINDEX : Sqlite3ActionCode(27)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object ANALYZE : Sqlite3ActionCode(28)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Module Name      |
     */
    public data object VTABLE : Sqlite3ActionCode(29)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | Module Name      |
     */
    public data object DROP_VTABLE : Sqlite3ActionCode(30)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | NULL             | Function Name    |
     */
    public data object FUNCTION : Sqlite3ActionCode(31)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Operation        | Savepoint Name   |
     */
    public data object SAVEPOINT : Sqlite3ActionCode(32)

    /**
     * No longer used.
     */
    public data object COPY : Sqlite3ActionCode(0)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | NULL             | NULL             |
     */
    public data object RECURSIVE : Sqlite3ActionCode(33)
}