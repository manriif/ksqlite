package tools.sqlitemc

import komple.platform.Host
import komple.tool.configurator.DefaultKompleToolConfigurator
import komple.tool.extension.ExtensionConfigurationScope
import komple.tool.extension.createExtension
import javax.inject.Inject

/**
 * Configurator for Sqlite Multiple Ciphers.
 */
abstract class SqliteMCConfigurator @Inject constructor(toolName: String) :
    DefaultKompleToolConfigurator<SqliteMCExtension>(toolName) {

    override fun supportHost(host: Host): Boolean = when (host.operatingSystem) {
        Linux, MacOS, Windows -> true
    }

    override fun ExtensionConfigurationScope<SqliteMCExtension>.configureExtension(): SqliteMCExtension {
        return createExtension()
    }
}