package ksqlite.gradle.wasm

import org.gradle.api.provider.Property

/**
 * Configuration of the WASM application.
 */
public interface KsqliteWasm {

    /**
     * Whether the application intend to use the Origin Private File System as an SQLite Virtual
     * File System.
     */
    //public val enableOpfsVfs: Property<Boolean>

    /**
     * The test runner used by the application.
     * Can be set to null to not generate a configuration for the runner.
     *
     * Default to null.
     */
    public val testRunner: Property<WasmTestRunner>
}