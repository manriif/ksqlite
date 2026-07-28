package ksqlite.structs

/**
 * Member of the struct [Type].
 */
public interface StructMember<Type> {

    /**
     * Position of the member in the struct.
     */
    public val ordinal: Int
}