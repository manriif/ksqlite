/**
 * @type {KsqliteModuleConfig}
 */
export const ksqliteModuleConfig = {
    customDebugModule: console.log,
    customLocateFile: (path, _) => `base/kotlin/${path}`
}