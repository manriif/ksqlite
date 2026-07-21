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
import sqliteInitModule from "./ksqlite.mjs";

/**
 * Environment variables for custom configuration.
 *
 * @typedef {Object} KsqliteEnv
 *
 * @property {boolean} isTest
 * Whether it is a test environment.
 *
 * @property {string} prefix
 * The prefix for file location in test mode.
 *
 * @type {KsqliteEnv}
 */
const env = window.__karma__?.config?.env?.ksqlite ?? {
    isTest: false,
    prefix: "ksqlite/"
};

/**
 * Configuration for the Ksqlite module.
 *
 * @typedef {Object} KsqliteModuleConfig
 *
 * @property {((...args: unknown) => void) | undefined} customDebugModule
 * Optional callback invoked for logging messages.
 *
 * @property {((path: string, prefix: string) => unknown) | undefined} customLocateFile
 * Optional callback used to resolve wasm file location.
 *
 * @type {KsqliteModuleConfig}
 */
const moduleConfig = {
    customDebugModule: env.isTest ? console.log : undefined,
    customLocateFile: (path, _) => `${env.prefix}${path}`
};

const sqliteInit = sqliteInitModule(moduleConfig);

// Force Karma to wait for SQLite initialization, otherwise tests fails in Kotlin/JS
if (env.isTest && window.__karma__) {
    const karma = window.__karma__;
    const karmaStart = karma.start.bind(karma);

    karma.start = (...args) => {
        sqliteInit.then(
            () => karmaStart(...args),
            (err) => karma.error("ksqlite init failed: " + (err?.stack ?? err))
        );
    };
}

/**
 * SQLite instance.
 */
export const sqlite3 = await sqliteInit;