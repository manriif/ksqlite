package ksqlite.memory

import ksqlite.convertResult
import ksqlite.types.Sqlite3Result

/**
 * Base for pointer of sqlite3_* types.
 */
public abstract class PointerBase<Pointer : Any, PointerPointer> internal constructor() {

    private var state: State<Pointer> = State.Created
    private var _memory = lazy(::MemoryManager)
    internal val memory by _memory

    internal val pointer: Pointer
        get() = when (val state = state) {
            is State.Allocated -> checkNotNull(state.pointer) {
                "The struct allocation failed, check the SQLite documentation to know if the " +
                        "finalizer function for the struct should be called"
            }

            State.Created -> error("The API to allocate the struct has not been called")
            State.Deallocated -> error("The struct has been deallocated")
        }

    ///////////////////////////////////////////////////////////////////////////
    // Memory
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Invokes [allocate] to let SQLite makes the actual allocation of [Pointer] and returns the
     * SQLite returned result.
     *
     * When the [pointer] is available, invokes [handlePointer] with it whether [allocate]
     * succeeded.
     */
    protected abstract fun doAllocation(
        allocate: (PointerPointer) -> Int,
        handlePointer: (Pointer?) -> Unit
    ): Int

    /**
     * Invokes [block] letting SQLite allocating memory to hold [Struct].
     */
    internal fun allocate(block: (PointerPointer) -> Int): Sqlite3Result {
        val currentState = state

        if (currentState != State.Created) {
            error(
                when (currentState) {
                    is State.Allocated -> if (currentState.pointer == null) {
                        "The struct has already been allocated"
                    } else {
                        "The struct allocation failed and it is not safe to allocate it again, " +
                                "check the SQLite documentation to know what to do next"
                    }

                    State.Deallocated -> "The struct has been deallocated, this pointer instance " +
                            "should no longer be used"
                }
            )
        }

        var pointer: Pointer? = null
        val result = convertResult(doAllocation(block) { pointer = it })

        if (result == Sqlite3Result.OK) {
            state = State.Allocated(checkNotNull(pointer) {
                "Unexpected null pointer value while SQLite succeeded"
            })
        } else {
            check(pointer == null) { "Unexpected pointer value while SQLite failed" }
            state = State.Allocated(null)
        }

        return result
    }

    /**
     * Invokes [block] to deallocate memory previously allocated by SQLite.
     * If a previous attempt failed, the pointer passed to [block] is `null`.
     * If the deallocation fails, the state of the pointer stays unchanged.
     *
     * If accessed and deallocation succeed, [memory] is closed.
     */
    internal fun deallocate(block: (Pointer?) -> Int): Sqlite3Result {
        val currentState = state

        if (currentState !is State.Allocated) {
            error(
                when (currentState) {
                    is State.Created -> "The struct must be allocated first, which was not done"
                    State.Deallocated -> "The struct has already been deallocated, this pointer " +
                            "instance should no longer be used"
                }
            )
        }

        val result = convertResult(block(currentState.pointer))

        if (result == Sqlite3Result.OK) {
            if (_memory.isInitialized()) {
                memory.close()
            }

            state = State.Deallocated
        }

        return result
    }

    ///////////////////////////////////////////////////////////////////////////
    // State
    ///////////////////////////////////////////////////////////////////////////

    /**
     * State of the [Pointer].
     */
    internal sealed interface State<out Pointer> {

        /**
         * Pointer was created but not yet allocated.
         */
        object Created : State<Nothing>

        /**
         * An attempt to allocate [Pointer] was done.
         * The [pointer] is `null` if SQLite allocation failed and not `null` otherwise.
         */
        class Allocated<Pointer>(val pointer: Pointer?) : State<Pointer>

        /**
         * Pointer was deallocated.
         */
        object Deallocated : State<Nothing>
    }
}