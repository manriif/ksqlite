package ksqlite

/**
 * Deleter for a resource [Target].
 */
public sealed interface Destructor<out Target> {

    /**
     * Provides default instances of [Destructor].
     */
    public companion object {

        /**
         * Returns a special [Destructor] instance causing the target pointer not to be destroyed.
         */
        public val Static: Destructor<Nothing>
            get() = SpecialDestructor.Static

        /**
         * Returns a special [Destructor] instance telling SQLite to make a private copy of the
         * content before returning.
         */
        public val Transient: Destructor<Nothing>
            get() = SpecialDestructor.Static

        /**
         * Returns a [Destructor] instance that will invoke [destruct] to destruct instance of
         * [Target].
         */
        public operator fun <Target> invoke(destruct: (Target) -> Unit): Destructor<Target> {
            return DestructorFunction(destruct)
        }
    }
}

/**
 * Holds a function that will destruct [Target].
 */
internal class DestructorFunction<Target>(val destructor: (Target) -> Unit) : Destructor<Target>

/**
 * Constants defining special destructor behavior.
 * These are special values for the destructor that is passed in as the final argument to routines.
 *
 * (special deleters)(https://sqlite.org/c3ref/c_static.html]
 */
public sealed class SpecialDestructor(internal val constant: Int) : Destructor<Nothing> {

    /**
     * Content pointer is constant and will never change and  does not need to be destroyed.
     */
    public data object Static : SpecialDestructor(0)

    /**
     * Content will likely change in the near future and that SQLite should make its own private
     * copy of the content before returning.
     */
    public data object Transient : SpecialDestructor(-1)
}