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
package ksqlite.types

/**
 * Available modes for prepared statement explain setting.
 *
 * [Change The EXPLAIN Setting For A Prepared Statement](https://sqlite.org/c3ref/stmt_explain.html)
 */
public enum class SqliteExplainMode(public val id: Int) {

    /**
	 * Statement becomes a normal prepared statement.
	 */
	NORMAL(0),

    /**
	 * Statement behaves as if its SQL text began with "EXPLAIN".
	 */
	EXPLAIN(1),

    /**
	 * Statement behaves as if its SQL text began with "EXPLAIN QUERY PLAN".
	 */
	EXPLAIN_QUERY_PLAN(2),
}