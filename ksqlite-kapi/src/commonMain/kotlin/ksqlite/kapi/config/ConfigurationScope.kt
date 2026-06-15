package ksqlite.kapi.config

public interface ConfigurationScope : AnyTimeConfigurationScope {

    public fun singleThread()

    public fun multiThread()

    public fun serialized()

    public fun pageCache()
}