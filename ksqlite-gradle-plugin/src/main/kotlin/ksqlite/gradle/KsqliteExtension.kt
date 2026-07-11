package ksqlite.gradle

import ksqlite.gradle.wasm.KsqliteWasm
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

/**
 * Extension for the Ksqlite Gradle Plugin.
 */
public abstract class KsqliteExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Wasm resources configuration.
     */
    public val wasm: KsqliteWasm = objects.newInstance<KsqliteWasm>()
}

///////////////////////////////////////////////////////////////////////////
// Conventions
///////////////////////////////////////////////////////////////////////////

/**
 * Configures this [KsqliteExtension].
 */
internal fun KsqliteExtension.configure() {
    wasm.run {
        //enableOpfsVfs.convention(false)
        testRunner.convention(null)
    }

    (this as ExtensionAware).extensions.run {
        add(KsqliteExtension::wasm.name, wasm)
    }
}