package ksqlite.kapi.value

/**
 * Holder for a status-like value and its highwater.
 */
public interface Status {

    /**
     * Current value.
     */
    public val current: Long

    /**
     * Highest value before reset.
     */
    public val highwater: Long
}