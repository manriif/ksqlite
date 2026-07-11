import sqliteInitModule from "./ksqlite.mjs";

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
    customDebugModule: undefined,
    customLocateFile: undefined
};

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
    prefix: ""
};

if (env.isTest) {
    moduleConfig.customDebugModule = console.log
    moduleConfig.customLocateFile = (path, _) => `${env.prefix}/${path}`
}

/**
 * SQLite instance.
 */
export const sqlite3 = await sqliteInitModule(moduleConfig);