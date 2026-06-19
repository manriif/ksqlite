package ksqlite.kapi.value

/**
 * Holder for a status-like value and its highwater.
 */
public interface StatusValue {

    /**
     * Current value.
     */
    public val current: Long

    /**
     * Highest value before reset.
     */
    public val highwater: Long
}