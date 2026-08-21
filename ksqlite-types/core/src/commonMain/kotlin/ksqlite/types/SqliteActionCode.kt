/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * Action that implies change in row(s).
     */
    public sealed class RowChange(code: Int) : SqliteActionCode(code)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object CREATE_INDEX : SqliteActionCode(1)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object CREATE_TABLE : SqliteActionCode(2)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Index Name       | Table Name       |
     */
    public data object CREATE_TEMP_INDEX : SqliteActionCode(3)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object CREATE_TEMP_TABLE : SqliteActionCode(4)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object CREATE_TEMP_TRIGGER : SqliteActionCode(5)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object CREATE_TEMP_VIEW : SqliteActionCode(6)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Trigger Name     | Table Name       |
     */
    public data object CREATE_TRIGGER : SqliteActionCode(7)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | View Name        | NULL             |
     */
    public data object CREATE_VIEW : SqliteActionCode(8)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Table Name       | NULL             |
     */
    public data object DELETE : RowChange(9)

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
    public data object INSERT : RowChange(18)

    /**
     * | 3rd              | 4th              |
     * | ---------------- | ---------------- |
     * | Pragma Name      | 1st arg or NULL  |
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
    public data object UPDATE : RowChange(23)

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
    public data object CREATE_VTABLE : SqliteActionCode(29)

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