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
package ksqlite.kapi.config

/**
 * Receives SQLite's internal log messages once registered through
 * [ksqlite.kapi.config.AnyTimeConfiguration.setLogger].
 */
public fun interface Logger {

    /**
     * Called for each logged message, with the associated result code in [errorCode].
     */
    public fun log(
        errorCode: Int,
        message: String?
    )
}