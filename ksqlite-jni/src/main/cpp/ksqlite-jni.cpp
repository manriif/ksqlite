#include <cstdlib>
#include <cstring>
#include <limits>
#include <jni.h>

#include "ksqlite.h"
#include "utils/Unicode.h"

#include <unordered_map>

// FIXME I do not know if the IDE is broken but this is declared in <jni.h> but IDE complain that
//  it does not exists
typedef uint8_t jboolean;

///////////////////////////////////////////////////////////////////////////
// Classes
///////////////////////////////////////////////////////////////////////////

#define JAVA_INT "java.lang.Integer"
#define JAVA_LONG "java.lang.Long"

#define KSQLITE_JNI_EXCEPTION "KsqliteJniException"
#define DESTRUCTOR_CALLBACK "DestructorCallback"
#define AUTO_EXTENSION_CALLBACK "AutoExtensionCallback"
#define AUTO_VACUUM_PAGES_CALLBACK "AutoVacuumPagesCallback"
#define BUSY_HANDLER_CALLBACK "BusyHandlerCallback"
#define COLLATION_NEEDED_CALLBACK "CollationNeededCallback"
#define OUTPUT_POINTER "OutputPointer"

///////////////////////////////////////////////////////////////////////////
// Exceptions
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes JNI FatalError.
 *
 * For now this is used wherever a coding error is made or when an out of memory error occur.
 * This behaviour may change in the future and all functions calling this may be reimplemented to
 * handle things correctly.
 */
#define FatalError(M) env->FatalError(M)

/**
 * Raises a fatal error if pointer is not null.
 */
static inline void requireNull(
    JNIEnv* const env,
    void* pointer
) {
    if (pointer != nullptr) {
        FatalError("Expected object to be null");
    }
}

/**
 * Raises a fatal error if pointer is null.
 */
static inline void* requireNonNull(
    JNIEnv* const env,
    void* pointer
) {
    if (pointer == nullptr) {
        FatalError("Expected object not to be null");
    }

    return pointer;
}

/**
 * Raises a fatal error if object is null.
 */
static inline jobject requireNonNullJobject(
    JNIEnv* const env,
    jobject object
) {
    return static_cast<jobject>(requireNonNull(env, object));
}

#define RequireNull(P) requireNull(env, (P))
#define RequireNonNull(P) requireNonNull(env, (P))
#define RequireNonNullJobject(O) requireNonNullJobject(env, (O))

/**
 * Raises a fatal error if there is a pending exception.
 */
static inline void exceptionClearAndAbort(
    JNIEnv* const env,
    const char* errorMessage
) {
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        FatalError(errorMessage);
    }
}

#define ExceptionClearAndAbort(M) exceptionClearAndAbort(env, (M))

/**
 * Raises a fatal error if expression E is false (due to a lack of memory).
 *
 * TODO do not throw, all out of memory checks must be handled correctly to align with other
 *  platform's behaviour.
 */
#define OutOfMemoryCheck(E) if (!(E)) FatalError("KSQLite JNI is out of memory.")

/**
 * Returns true if an exception has been reported.
 */
static inline bool reportException(
    JNIEnv* const env,
    bool clear
) {
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();

        if (clear) {
            env->ExceptionClear();
        }

        return true;
    }

    return false;
}

#define IfExceptionThrownClear(clear) if (reportException(env, clear))
#define IfExceptionThrown IfExceptionThrownClear(false)
#define IfExceptionNotThrownClear(clear) if (!reportException(env, clear))
#define IfExceptionNotThrown IfExceptionNotThrownClear(false)

///////////////////////////////////////////////////////////////////////////
// JNI reference management helpers
///////////////////////////////////////////////////////////////////////////

static jobject newGlobalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object == nullptr) {
        return nullptr;
    }

    const auto ref = env->NewGlobalRef(object);
    OutOfMemoryCheck(ref != nullptr);
    return ref;
}

static jobject newLocalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object == nullptr) {
        return nullptr;
    }

    const auto ref = env->NewLocalRef(object);
    OutOfMemoryCheck(ref != nullptr);
    return ref;
}

static inline void deleteGlobalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object != nullptr) {
        env->DeleteGlobalRef(object);
    }
}

static inline void deleteLocalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object != nullptr) {
        env->DeleteLocalRef(object);
    }
}

#define GlobalRefCreate(O) newGlobalReference(env, (O))
#define LocalRefCreate(O) newLocalReference(env, (O))
#define GlobalRefDestroy(O) deleteGlobalReference(env, (O))
#define LocalRefDestroy(O) deleteLocalReference(env, (O))

///////////////////////////////////////////////////////////////////////////
// JNI classes, methods, fields helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Common type for java classes.
 */
struct Class {
    jclass klass;
};

/**
 * Clears a Class.
 */
static inline void clearClass(
    JNIEnv* env,
    Class* klass
) {
    GlobalRefDestroy(klass->klass);
    klass->klass = nullptr;
}

#define ClassClear(klass) clearClass(env, &klass)

static inline jclass getClassOrDie(
    JNIEnv* const env,
    const char* name,
    const char* errorMessage
) {
    const auto klass = GlobalRefCreate(env->FindClass(name));
    ExceptionClearAndAbort(errorMessage);
    return reinterpret_cast<jclass>(klass);
}

#define RequireClass(klassName) \
    getClassOrDie(env, klassName, "Error getting reference to " klassName " class")

#define RequireKsqliteClass(klassName) RequireClass("ksqlite/" klassName)

/**
 * Raises a fatal error when a method was not found on a given class.
 */
static inline jmethodID getMethodIdOrDie(
    JNIEnv* const env,
    jclass klass,
    const char* name,
    const char* signature,
    const char* errorMessage
) {
    const auto methodId = env->GetMethodID(klass, name, signature);
    ExceptionClearAndAbort(errorMessage);
    return methodId;
}

#define RequireClassMethod(klass, name, signature, className) \
    getMethodIdOrDie(env, klass, name, signature, \
    "Error getting reference to " className "#" name " method")

#define RequireMethod(O, name, signature, className) \
    RequireClassMethod(O.klass, name, signature, className)

#define RequireKsqliteClassMethod(klass, name, signature, className) \
    RequireClassMethod(klass, name, signature, "ksqlite." className)

#define RequireKsqliteMethod(O, name, signature, className) \
    RequireKsqliteClassMethod(O.klass, name, signature, className)

/**
 * Raises a fatal error when a field was not found on a given class.
 */
static inline jfieldID getFieldIdOrDie(
    JNIEnv* const env,
    jclass klass,
    const char* name,
    const char* signature,
    const char* errorMessage
) {
    const auto fieldId = env->GetFieldID(klass, name, signature);
    ExceptionClearAndAbort(errorMessage);
    return fieldId;
}

#define RequireClassField(klass, name, signature, className) \
    getFieldIdOrDie(env, klass, name, signature, \
    "Error getting reference to " className "#" name " field")

#define RequireField(O, name, signature, className) \
    RequireClassField(O.klass, name, signature, className)

#define RequireKsqliteClassField(klass, name, signature, className) \
    RequireClassField(klass, name, signature, "ksqlite." className)

#define RequireKsqliteField(O, name, signature, className) \
    RequireKsqliteClassField(O.klass, name, signature, className)

///////////////////////////////////////////////////////////////////////////
// Mutex
///////////////////////////////////////////////////////////////////////////

/**
 * Common type for object guarded by a mutex.
 */
struct MutexGuarded {
    sqlite3_mutex* mutex;
};

#define MutexAllocate(O) \
    O.mutex = sqlite3_mutex_alloc(SQLITE_MUTEX_FAST); \
    OutOfMemoryCheck(O.mutex)

#define MutexDestroy(O) sqlite3_mutex_free(O.mutex)
#define MutexEnter(O) sqlite3_mutex_enter(O.mutex)
#define MutexLeave(O) sqlite3_mutex_leave(O.mutex)

///////////////////////////////////////////////////////////////////////////
// Destructor
///////////////////////////////////////////////////////////////////////////

/**
 * Destructor function type.
 */
typedef void(* DestructorFunction)(void*);

/**
 * Holder for Java object that can be destroyed.
 */
struct Destroyable {
    jobject destructor;
};

/**
 * Clears a Destroyable.
 */
static inline void clearDestroyable(
    JNIEnv* env,
    Destroyable* destroyable
) {
    GlobalRefDestroy(destroyable->destructor);
    destroyable->destructor = nullptr;
}

#define DestroyableClear(destroyable) clearHook(env, &destroyable)

///////////////////////////////////////////////////////////////////////////
// Hooks
///////////////////////////////////////////////////////////////////////////

/**
 * Holder for a Java object with a call method.
 */
struct Hook {
    jobject instance;
    jmethodID call;
};

/**
 * Holder for a Java object with a call method and a java object that can be destroyed.
 */
struct HookDestroyable : Hook, Destroyable {
};

/**
 * Configures a hook.
 */
#define HookConfigure(hook, object, signature, className) \
    const auto klass = env->GetObjectClass(object); \
    hook.call = RequireKsqliteClassMethod(klass, "call", signature, className); \
    hook.instance = GlobalRefCreate(object); \
    LocalRefDestroy(klass)

/**
 * Configures a hook and the associated destructor.
 */
#define HookDestroyableConfigure(hook, destructor, instance, signature, className) \
    if (destructor != nullptr) hook.destructor = GlobalRefCreate(destructor); \
    HookConfigure(hook, instance, signature, className)

/**
 * Declares the common variables for hook call method invocation.
 * The instance is wrapped into a local reference that can be released when no longer required.
 */
#define HookEnter(guard, hook) \
    MutexEnter(guard); \
    const auto instance = LocalRefCreate(RequireNonNullJobject(hook.instance)); \
    const auto call = hook.call; \
    MutexLeave(guard)

#define HookLeave() LocalRefDestroy(instance)
#define HookEnterGlobal(hook) HookEnter(KHS, KHS.hook)
#define HookEnterDbState(hook) HookEnter(dbState, dbState.hooks.hook)

/**
 * Clears a Hook.
 */
static inline void clearHook(
    JNIEnv* env,
    Hook* hook
) {
    GlobalRefDestroy(hook->instance);
    hook->instance = nullptr;
    hook->call = nullptr;
}

#define HookClear(hook) clearHook(env, &hook)

///////////////////////////////////////////////////////////////////////////
// Freeable
///////////////////////////////////////////////////////////////////////////

/**
 * Holder for an optional pointer allocated with sqlite3_malloc(), an optional Java object to keep
 * globally reachable and an optional globally referenced destructor.
 */
struct Freeable : Destroyable {
    void* pointer;
    jobject target;
};

typedef std::unordered_map<void*, Freeable*> FreeableMap;

/**
 * Allocates a new Freeable if at least one of pointer, target or destructor is not null. Returns
 * null if none of the supplied arguments is not null.
 */
static Freeable* allocateFreeable(
    JNIEnv* env,
    void* pointer,
    jobject target,
    jobject destructor
) {
    if (pointer == nullptr && target == nullptr && destructor == nullptr) {
        return nullptr;
    }

    jobject globalTarget = nullptr;
    jobject globalDestructor = nullptr;

    if (target != nullptr) {
        globalTarget = GlobalRefCreate(target);
    }

    if (destructor != nullptr) {
        globalDestructor = GlobalRefCreate(destructor);
    }

    return new Freeable { { globalDestructor }, pointer, globalTarget };
}

/**
 * Calls the Java destructor for the given Freeable and releases associated resources.
 * The pointer must have been allocated with `new`.
 */
static void destroyFreeable(
    JNIEnv* env,
    void* ptrToFreeable,
    jmethodID destroy
) {
    const auto freeablePtr = reinterpret_cast<Freeable*>(RequireNonNull(ptrToFreeable));
    auto freeable = *freeablePtr;

    jobject destructor = LocalRefCreate(freeable.destructor);

    if (destructor != nullptr) {
        GlobalRefDestroy(freeable.destructor);
        env->CallVoidMethod(destructor, destroy);
        LocalRefDestroy(destructor);
    }

    if (freeable.target != nullptr) {
        GlobalRefDestroy(freeable.target);
    }

    if (freeable.pointer != nullptr) {
        sqlite3_free(freeable.pointer);
    }

    delete freeablePtr;
}

#define AllocateFreeable(pointer, target, destructor) \
    allocateFreeable(env, pointer, target, destructor)

#define AllocateFreeablePointer(pointer, destructor) \
    AllocateFreeable(pointer, nullptr, destructor)

#define AllocateFreeableTarget(target, destructor) \
    AllocateFreeable(nullptr, target, destructor)

///////////////////////////////////////////////////////////////////////////
// Database
///////////////////////////////////////////////////////////////////////////

/**
 * Database connection state.
 */
struct DbState : MutexGuarded {

    struct {
        HookDestroyable autoVacuumPages;
        Hook busyHandler;
        Hook collationNeeded;
        /*S3JniHook commit;
        S3JniHook progress;
        S3JniHook rollback;
        S3JniHook trace;
        S3JniHook update;
        S3JniHook auth;*/
    } hooks;
};

typedef std::unordered_map<sqlite3*, DbState*> DbStateMap;

///////////////////////////////////////////////////////////////////////////
// Global State
///////////////////////////////////////////////////////////////////////////

/**
 * Global state.
 */
static struct {
    JavaVM* jvm;

    // Holds database connection states
    struct : MutexGuarded {
        DbStateMap* map;
    } dbs;

    // Holds Freeable object associated by a pointer passed to sqlite as `user_data`
    struct : MutexGuarded {
        FreeableMap* map;
    } freeables;

    struct {
        jclass illegalArgumentException;
        jclass illegalStateException;

        struct : Class {
            jmethodID constructor; // (I)V
            jmethodID intValue; // ()I
        } int32; // Integer

        struct : Class {
            jmethodID constructor; //(J)V
            jmethodID longValue; // ()J
        } int64; // Long
    } java;

    // KsqliteJni classes
    struct {
        struct : Class {
            jmethodID destroy; // ()V
        } destructorCallback;

        struct : Class {
            jmethodID resultCode; // ()I
            jmethodID message; // ()Ljava/lang/String;
        } jniException;

        struct : Class {
            jfieldID value; // Ljava/lang/Object;
        } outputPointer;
    } ksqlite;

    // Global hooks
    struct : MutexGuarded {
        Hook autoExtension;
    } hooks;
} KsqliteJniGlobalState;

#define K KsqliteJniGlobalState
#define KDS K.dbs
#define KFS K.freeables
#define KHS K.hooks

#define KJV K.java
#define KJVI K.java.int32
#define KJVL K.java.int64

#define KKDC K.ksqlite.destructorCallback
#define KKJE K.ksqlite.jniException
#define KKOP K.ksqlite.outputPointer

///////////////////////////////////////////////////////////////////////////
// JNI Environment
///////////////////////////////////////////////////////////////////////////

#define JNI_VERSION_1_8 0x00010008
#define JNI_VERSION JNI_VERSION_1_8

/**
 * Returns the current JNIEnv object or abort if it cannot find the object.
 */
static JNIEnv* retrieveJniEnv() {
    JNIEnv* env = nullptr;
    const auto result = K.jvm->GetEnv(reinterpret_cast<void**>(env), JNI_VERSION);

    if (result != JNI_OK) {
        if (result == JNI_EDETACHED) {
            fprintf(stderr, "Fatal error: thread is not attached.\n");
        } else {
            fprintf(stderr, "Fatal error: cannot get current JNIEnv.\n");
        }

        abort();
    }

    return env;
}

/**
 * Declares the JNIEnv in the function body.
 */
#define JniEnvDeclare() const auto env = retrieveJniEnv()

///////////////////////////////////////////////////////////////////////////
// Lifecycle
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes and caches the Java related classes and objects.
 */
static void initializeJavaJniCache(JNIEnv* env) {
    // Exceptions
    KJV.illegalArgumentException = RequireClass("java/lang/IllegalArgumentException");
    KJV.illegalStateException = RequireClass("java/lang/IllegalStateException");

    // Integer
    KJVI.klass = RequireClass("java/lang/Integer");
    KJVI.constructor = RequireMethod(KJVI, "<init>", "(I)V", JAVA_INT);
    KJVI.intValue = RequireMethod(KJVI, "intValue", "()I", JAVA_INT);

    // Long
    KJVL.klass = RequireClass("java/lang/Long");
    KJVL.constructor = RequireMethod(KJVL, "<init>", "(J)V", JAVA_LONG);
    KJVL.longValue = RequireMethod(KJVL, "longValue", "()J", JAVA_LONG);
}

/**
 * Deinitializes cached Java related classes and objects.
 */
static void deinitializeJavaJniCache(JNIEnv* env) {
    ClassClear(KJVL);
    KJVL.constructor = nullptr;
    KJVL.longValue = nullptr;

    ClassClear(KJVI);
    KJVI.constructor = nullptr;
    KJVI.intValue = nullptr;

    GlobalRefDestroy(KJV.illegalStateException);
    GlobalRefDestroy(KJV.illegalArgumentException);
}

/**
 * Initializes and caches the Ksqlite related classes and objects.
 */
static void initializeKsqliteJniCache(JNIEnv* env) {
    // DestructorCallback
    KKDC.klass = RequireKsqliteClass(DESTRUCTOR_CALLBACK);
    KKDC.destroy = RequireKsqliteMethod(KKDC, "destroy", "()V", DESTRUCTOR_CALLBACK);

    // KsqliteJniException
    KKJE.klass = RequireKsqliteClass(KSQLITE_JNI_EXCEPTION);
    KKJE.resultCode = RequireKsqliteMethod(KKJE, "getResultCode", "()I", KSQLITE_JNI_EXCEPTION);

    KKJE.message =
        RequireKsqliteMethod(KKJE, "getMessage", "()Ljava/lang/String;", KSQLITE_JNI_EXCEPTION);

    // OutputPointer
    KKOP.klass = RequireKsqliteClass(OUTPUT_POINTER);
    KKOP.value = RequireKsqliteField(KKOP, "value", "Ljava/lang/Object;", OUTPUT_POINTER);
}

/**
 * Deinitializes cached Ksqlite related classes and objects.
 */
static void deinitializeKsqliteJniCache(JNIEnv* env) {
    ClassClear(KKOP);
    KKOP.value = nullptr;

    ClassClear(KKJE);
    KKJE.message = nullptr;
    KKJE.resultCode = nullptr;

    ClassClear(KKDC);
    KKDC.destroy = nullptr;
}

/**
 * Initializes and cache global mutexes.
 */
static void initializeMutexes(JNIEnv* env) {
    MutexAllocate(KDS);
    MutexAllocate(KFS);
    MutexAllocate(KHS);
}

/**
 * Deinitializes cached global mutexes.
 */
static void deinitializeMutexes(JNIEnv* env) {
    MutexDestroy(KHS);
    MutexDestroy(KFS);
    MutexDestroy(KDS);
}

///////////////////////////////////////////////////////////////////////////
// JNI Lifecycle
//
// TODO move at the bottom of the file after all functions getting generated
///////////////////////////////////////////////////////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad(
    JavaVM* vm,
    void* reserved
) {
    K.jvm = vm;
    KDS.map = new DbStateMap();
    KFS.map = new FreeableMap();

    JniEnvDeclare();
    initializeJavaJniCache(env);
    initializeKsqliteJniCache(env);
    sqlite3_initialize();
    initializeMutexes(env);
    sqlite3_shutdown();

    return JNI_VERSION;
}

JNIEXPORT void JNICALL
JNI_OnUnload(
    JavaVM* vm,
    void* reserved
) {
    JniEnvDeclare();

    if (!KDS.map->empty()) {
        fprintf(stderr, "Database connections were not closed correctly.\n");
    }

    if (!KFS.map->empty()) {
        fprintf(stderr, "Statements were not cleaned up correctly.\n");
    }

    deinitializeMutexes(env);
    deinitializeKsqliteJniCache(env);
    deinitializeJavaJniCache(env);

    delete KFS.map;
    delete KDS.map;
    K.jvm = nullptr;
}

///////////////////////////////////////////////////////////////////////////
// Database connection operations
///////////////////////////////////////////////////////////////////////////

#define DbStateNotFoundFatalError() \
    FatalError("No state exists for the given database, pushDbState may have not been called")

/**
 * Creates and inserts the state of a newly created database connection.
 */
static DbState* pushDbState(
    JNIEnv* env,
    sqlite3* db
) {
    auto state = DbState();
    MutexAllocate(state);

    MutexEnter(KDS);
    const auto [iterator, inserted] = KDS.map->emplace(db, new DbState(state));
    MutexLeave(KDS);

    if (!inserted) {
        FatalError("Database state is already initialized");
    }

    return iterator->second;
}

/**
 * Removes and cleanups the state of a closed database connection.
 */
static void popDbState(
    JNIEnv* env,
    sqlite3* db
) {
    MutexEnter(KDS);

    const auto& map = KDS.map;
    const auto iterator = map->find(db);

    if (iterator == map->end()) {
        DbStateNotFoundFatalError();
    }

    const auto pState = iterator->second;
    auto state = *pState;

    MutexDestroy(state);
    delete pState;

    map->erase(iterator);
    MutexLeave(KDS);
}

/**
 * Returns the state of the supplied database.
 */
static DbState* getDbState(
    JNIEnv* env,
    sqlite3* db
) {
    MutexEnter(KDS);

    const auto& map = KDS.map;
    const auto iterator = KDS.map->find(db);

    if (iterator == map->end()) {
        DbStateNotFoundFatalError();
    }

    const auto state = iterator->second;
    MutexLeave(KDS);

    return state;
}

#define DbStatePush(db) pushDbState(env, db)
#define DbStatePop(db) popDbState(env, db)
#define DbStateGet(db) getDbState(env, db)

/**
 * Declares the variables to the database state in a hook caller function.
 */
#define DbStateDeclare(P) \
    const auto pDbState = reinterpret_cast<DbState*>(P); \
    auto dbState = *pDbState \

/**
 * Returns the state of the supplied database and enter its mutex.
 */
#define DbStateMutexEnter(db) \
    const auto pDbState = DbStateGet(db); \
    auto dbState = *pDbState; \
    MutexEnter(dbState)

/**
 * Leave the mutex from a previous call to DbStateMutexEnter().
 */
#define DbStateMutexLeave() MutexLeave(dbState)

/**
 * Declares the code needed to replace a database connection hook without destructor
 */
#define DbHookReplace(H, signature, klassName, function)        \
    const auto pDb = LongTo_s3(db);                             \
    auto rc = SQLITE_OK;                                        \
                                                                \
    DbStateMutexEnter(pDb);                                     \
    const auto pHook = &dbState.hooks.H;                        \
    auto& hook = *pHook;                                        \
                                                                \
    if (hook.instance != nullptr) {                             \
        HookClear(hook);                                        \
    }                                                           \
                                                                \
    if (callback != nullptr) {                                  \
        rc = function;                                          \
                                                                \
        if (rc == SQLITE_OK) {                                  \
            HookConfigure(hook, callback, signature, klassName);\
        }                                                       \
    }                                                           \
                                                                \
    DbStateMutexLeave();                                        \
    return rc

///////////////////////////////////////////////////////////////////////////
// Freeable operations
///////////////////////////////////////////////////////////////////////////

/**
 * Inserts a value into the freeable map.
 */
static void pushFreeable(
    JNIEnv* env,
    void* key,
    Freeable* value
) {
    MutexEnter(KFS);
    const auto [_, inserted] = KFS.map->emplace(key, value);
    MutexLeave(KFS);

    if (!inserted) {
        FatalError("An object already exists for the given key and may have not been cleaned up");
    }
}

/**
 * Removes and returns a value from the freeable map.
 */
static Freeable* popFreeable(
    JNIEnv* env,
    void* key
) {
    MutexEnter(KFS);

    const auto& map = KFS.map;
    const auto iterator = map->find(key);

    if (iterator == map->end()) {
        FatalError("No value exists for the given key, it may have been cleaned up previously"
                   " and reference tracking may be broken");
    }

    const auto value = iterator->second;

    map->erase(iterator);
    MutexLeave(KFS);

    return value;
}

#define FreeablePush(key, value) pushFreeable(env, key, value)
#define FreeablePop(key) popFreeable(env, key)

///////////////////////////////////////////////////////////////////////////
// Casting
///////////////////////////////////////////////////////////////////////////

#define PtrToLong(P) reinterpret_cast<jlong>(P)
#define LongToPtr(L) reinterpret_cast<void*>(L)

#define LongCast(T, L) reinterpret_cast<T*>(L)
#define LongTo_s3(L) LongCast(sqlite3, (L))
#define LongTo_s3_backup(L) LongCast(sqlite3_backup, (L))
#define LongTo_s3_blob(L) LongCast(sqlite3_blob, (L))
#define LongTo_s3_context(L) LongCast(sqlite3_context, (L))
#define LongTo_s3_stmt(L) LongCast(sqlite3_stmt, (L))
#define LongTo_s3_value(L) LongCast(sqlite3_value, (L))

///////////////////////////////////////////////////////////////////////////
// Buffer helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the direct buffer address of the given buffer or raises an exception if the address
 * cannot be obtained.
 */
static inline void* bufferDirectAddress(
    JNIEnv* env,
    jobject buffer
) {
    if (buffer == nullptr) {
        return nullptr;
    }

    const auto address = env->GetDirectBufferAddress(buffer);

    if (address == nullptr) {
        // TODO ensure java NIO is supported
        FatalError("Failed to get direct buffer address");
    }

    return address;
}

#define BufferDirectAddress(buffer) bufferDirectAddress(env, buffer)

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_createByteBuffer(
    JNIEnv* env,
    jclass clazz,
    jlong pointer,
    jlong size
) {
    const auto address = LongToPtr(pointer);

    if (address == nullptr) {
        return nullptr;
    }

    return env->NewDirectByteBuffer(address, size);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferRead(
    JNIEnv* env,
    jclass clazz,
    jobject buffer,
    jint size,
    jlong sourceOffset,
    jint destinationOffset,
    jbyteArray destination
) {
    const auto sourceAddress = BufferDirectAddress(buffer);

    if (sourceAddress == nullptr) {
        return;
    }

    const auto destinationAddress =
        static_cast<jbyte*>(env->GetPrimitiveArrayCritical(destination, nullptr));

    if (destinationAddress == nullptr) {
        return;
    }

    memcpy(
        destinationAddress + destinationOffset,
        static_cast<jbyte*>(sourceAddress) + sourceOffset,
        size
    );

    env->ReleasePrimitiveArrayCritical(destination, destinationAddress, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferWrite(
    JNIEnv* env,
    jclass clazz,
    jobject buffer,
    jbyteArray source,
    jint size,
    jint sourceOffset,
    jlong destinationOffset
) {
    const auto destinationAddress = BufferDirectAddress(buffer);

    if (destinationAddress == nullptr) {
        return;
    }

    const auto sourceAddress =
        static_cast<jbyte*>(env->GetPrimitiveArrayCritical(source, nullptr));

    if (sourceAddress == nullptr) {
        return;
    }

    memcpy(
        static_cast<jbyte*>(destinationAddress) + destinationOffset,
        sourceAddress + sourceOffset,
        size
    );

    env->ReleasePrimitiveArrayCritical(source, sourceAddress, 0);
}

///////////////////////////////////////////////////////////////////////////
// ByteArray helpers
///////////////////////////////////////////////////////////////////////////

/*
 * Creates a new jByteArray of length size, copies buffer's contents into it, and returns that byte
 * array (NULL on OOM unless fail-fast alloc errors are enabled).
*/
static jbyteArray bufferToByteArray(
    JNIEnv* const env,
    const void* const buffer,
    int length
) {
    if (buffer == nullptr) {
        return nullptr;
    }

    jbyteArray byteArray = env->NewByteArray(length);
    OutOfMemoryCheck(byteArray != nullptr);
    env->SetByteArrayRegion(byteArray, 0, length, static_cast<const jbyte*>(buffer));

    return byteArray;
}

/**
 * Returns the elements from the given byteArray or null if byteArray is null.
 * The returned byteArray must be freed with sqlite3_free() when no longer required.
 */
static jbyte* byteArrayToBuffer(
    JNIEnv* const env,
    jbyteArray byteArray,
    int length
) {
    if (byteArray == nullptr) {
        return nullptr;
    }

    const auto buffer = static_cast<jbyte*>(sqlite3_malloc(length));
    OutOfMemoryCheck(buffer != nullptr);
    env->GetByteArrayRegion(byteArray, 0, length, buffer);

    return buffer;
}

#define BufferToByteArray(buffer, length) bufferToByteArray(env, buffer, length)
#define ByteArrayToBuffer(byteArray, length) byteArrayToBuffer(env, byteArray, length)

///////////////////////////////////////////////////////////////////////////
// String helpers
///////////////////////////////////////////////////////////////////////////

/*
 * Converts Java String (UTF-16) to UTF-8 C string using Android AOSP Unicode transcoder.
 * Output must be freed with sqlite3_free().
 *
 * Returns nullptr on error or null input.
 */
static char* jstringToUtf8(
    JNIEnv* env,
    jstring string,
    size_t* outLength
) {
    if (string == nullptr) {
        if (outLength != nullptr) {
            *outLength = 0;
        }

        return nullptr;
    }

    const auto chars = env->GetStringChars(string, nullptr);

    if (chars == nullptr) {
        return nullptr;
    }

    const auto length = env->GetStringLength(string);

    const auto utf8Length = utf16_to_utf8_length(
        reinterpret_cast<const char16_t*>(chars),
        length
    );

    if (utf8Length <= 0) {
        env->ReleaseStringChars(string, chars);
        return nullptr;
    }

    const auto utf8 = static_cast<char*>(sqlite3_malloc(utf8Length + 1));
    OutOfMemoryCheck(utf8);

    utf16_to_utf8(
        reinterpret_cast<const char16_t*>(chars),
        length,
        utf8,
        utf8Length + 1
    );

    env->ReleaseStringChars(string, chars);

    if (outLength != nullptr) {
        *outLength = utf8Length;
    }

    return utf8;
}

/*
 * Converts UTF-8 C string to Java String (UTF-16) using Android AOSP Unicode transcoder.
 */
static jstring utf8ToJstring(
    JNIEnv* env,
    const char* utf8,
    size_t length
) {
    if (utf8 == nullptr) {
        return nullptr;
    }

    if (length == -1) {
        // This is what is used by sqlite internally
        length = 0x3fffffff & static_cast<int>(strlen(utf8));
    }

    const auto utf16Length = utf8_to_utf16_length(
        reinterpret_cast<const uint8_t*>(utf8),
        length
    );

    if (utf16Length <= 0) {
        return env->NewString(nullptr, 0);
    }

    static const int jCharSize = sizeof(jchar);
    const auto utf16 = static_cast<jchar*>(sqlite3_malloc((utf16Length + 1) * jCharSize));

    if (utf16 == nullptr) {
        return nullptr;
    }

    utf8_to_utf16(
        reinterpret_cast<const uint8_t*>(utf8),
        length,
        reinterpret_cast<char16_t*>(utf16),
        utf16Length + 1
    );

    jstring string = env->NewString(utf16, utf16Length);
    sqlite3_free(utf16);

    return string;
}

#define JstringToUtf8Out(string, outLength) jstringToUtf8(env, string, outLength)
#define JstringToUtf8(string) JstringToUtf8Out(string, nullptr)
#define Utf8ToJstringLength(utf8, length) utf8ToJstring(env, utf8, length)
#define Utf8ToJstring(utf8) Utf8ToJstringLength(utf8, -1)

///////////////////////////////////////////////////////////////////////////
// Output pointers
///////////////////////////////////////////////////////////////////////////

/**
 * Gets the value of an OutputPointer.
 */
static jobject outputPointerGetValue(
    JNIEnv* env,
    jobject pointer
) {
    if (pointer == nullptr) {
        return nullptr;
    }

    const auto value = env->GetObjectField(pointer, KKOP.value);
    ExceptionClearAndAbort("Cannot get OutputPointer.value");

    return value;
}

/**
 * Sets the value of an OutputPointer.
 */
static void outputPointerSetValue(
    JNIEnv* env,
    jobject pointer,
    jobject value
) {
    if (pointer != nullptr) {
        env->SetObjectField(pointer, KKOP.value, value);
        ExceptionClearAndAbort("Cannot set OutputPointer.value");
    }
}

/**
 * Gets the value of a 32bits integer OutputPointer.
 */
static jint outputPointerGetInt32Value(
    JNIEnv* env,
    jobject pointer
) {
    const auto boxedInt = RequireNonNullJobject(outputPointerGetValue(env, pointer));
    const auto value = env->CallIntMethod(boxedInt, KJVI.intValue);
    ExceptionClearAndAbort("Failed to get the integer value from a boxed int");
    return value;
}

/**
 * Sets the value of a 32bits integer OutputPointer.
 */
static void outputPointerSetInt32Value(
    JNIEnv* env,
    jobject pointer,
    jint value
) {
    if (pointer != nullptr) {
        jobject boxedInt = env->NewObject(KJVI.klass, KJVI.constructor, value);
        outputPointerSetValue(env, pointer, boxedInt);
    }
}

/**
 * Gets the value of a 64bits integer OutputPointer.
 */
static jlong outputPointerGetInt64Value(
    JNIEnv* env,
    jobject pointer
) {
    const auto boxedLong = RequireNonNullJobject(outputPointerGetValue(env, pointer));
    const auto value = env->CallLongMethod(boxedLong, KJVL.longValue);
    ExceptionClearAndAbort("Failed to get the long value from a boxed long");
    return value;
}

/**
 * Sets the value of a 64bits integer OutputPointer.
 */
static void outputPointerSetInt64Value(
    JNIEnv* env,
    jobject pointer,
    jlong value
) {
    if (pointer != nullptr) {
        jobject boxedLong = env->NewObject(KJVL.klass, KJVL.constructor, value);
        outputPointerSetValue(env, pointer, boxedLong);
    }
}

#define OutputPointerGetValue(pointer) outputPointerGetValue(env, pointer)
#define OutputPointerSetValue(pointer, value) outputPointerSetValue(env, pointer, value)
#define OutputPointerGetInt32Value(pointer) outputPointerGetInt32Value(env, pointer)
#define OutputPointerSetInt32Value(pointer, value) outputPointerSetInt32Value(env, pointer, value)
#define OutputPointerGetInt64Value(pointer) outputPointerGetInt64Value(env, pointer)
#define OutputPointerSetInt64Value(pointer, value) outputPointerSetInt64Value(env, pointer, value)

#define OutputPointerGetPointerValue(T, pointer) \
    reinterpret_cast<T*>(OutputPointerGetInt64Value(pointer))

#define OutputPointerSetPointerValue(pointer, value) \
    OutputPointerSetInt64Value(pointer, PtrToLong(value))

///////////////////////////////////////////////////////////////////////////
// Parameters
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an IllegalArgumentException if the destructor argument is not null but argument is.
 */
#define DestructorCheck(argument, result) \
    if (argument == nullptr) { \
        env->ThrowNew(                    \
            KJV.illegalArgumentException, \
            "destructor must be null if " #argument " is null" \
        )         ;                        \
        return result; \
    }

///////////////////////////////////////////////////////////////////////////
// Callbacks
///////////////////////////////////////////////////////////////////////////

/**
 * Calls the Java destructor for the given Freeable pointer and releases associated resources.
 * The pointer must have been allocated with `new`.
 */
static void freeableDestroyer(void* ptrToFreeable) {
    destroyFreeable(retrieveJniEnv(), ptrToFreeable, KKDC.destroy);
}

/**
 * Retrieves the Freeable pointer associated with pointer and calls the Java destructor releasing
 * associated resources.
 */
static void freeableDestroyerPop(void* pointer) {
    const auto env = retrieveJniEnv();
    const auto ptrToFreeable = FreeablePop(pointer);
    destroyFreeable(env, ptrToFreeable, KKDC.destroy);
}

/**
 * Pushes freeable and returns the destructor function for it.
 * Returns null if freeable is null.
 */
static inline DestructorFunction freeableDestroyerPush(
    JNIEnv* env,
    void* key,
    Freeable* freeable
) {
    if (freeable == nullptr) {
        return nullptr;
    }

    FreeablePush(key, freeable);
    return freeableDestroyerPop;
}

/**
 * Returns freeableDestructor() function if given pointer P is not null.
 */
#define FreeableDestroyer(P) (P) == nullptr ? nullptr : freeableDestroyer
#define FreeableDestroyerPush(K, F) freeableDestroyerPush(env, K, F)

/**
 * Calls the Java destructor for the given hook and releases associated resources.
 */
static void hookDestroyer(void* ptrToHook) {
    JniEnvDeclare();
    const auto hookPtr = reinterpret_cast<HookDestroyable*>(ptrToHook);
    auto hook = *hookPtr;

    MutexEnter(KHS);

    // Instance is optional
    if (hook.instance != nullptr) {
        HookClear(hook);
    }

    jobject destructor = nullptr;

    // Destructor is also optional but required for callback
    if (hook.destructor != nullptr) {
        destructor = LocalRefCreate(RequireNonNullJobject(hook.destructor));
        DestroyableClear(hook);
    }

    MutexLeave(KHS);

    if (destructor != nullptr) {
        env->CallVoidMethod(destructor, KKDC.destroy);
        LocalRefDestroy(destructor);
    }
}

/**
 * Returns hookDestroy() callback if given pointer P is not null.
 */
#define HookDestroyer(P) if ((P) == nullptr) nullptr else hookDestroy

/**
 * Calls the auto_extension hook.
 */
static int autoExtensionCaller(
    sqlite3* pDb,
    char** pzErr,
    const sqlite3_api_routines* pApi
) {
    JniEnvDeclare();
    const auto dbPtr = PtrToLong(pDb);
    const auto apiPtr = PtrToLong(pApi);

    HookEnterGlobal(autoExtension);
    auto rc = env->CallIntMethod(instance, call, dbPtr, apiPtr);
    HookLeave();

    if (const auto exception = env->ExceptionOccurred(); exception != nullptr) {
        env->ExceptionClear();

        if (!env->IsInstanceOf(exception, KKJE.klass)) {
            FatalError("Unexpected exception type thrown in AutoExtensionCallback#call");
        }

        const auto message =
            reinterpret_cast<jstring>(env->CallObjectMethod(exception, KKJE.message));

        // Let Java handle theses unexpected method call exceptions
        IfExceptionThrown {
            rc = SQLITE_ERROR;
        } else {
            const auto utf8 = JstringToUtf8(message);

            if (utf8 != nullptr) {
                *pzErr = sqlite3_mprintf(utf8);
                sqlite3_free(message);
            }

            rc = env->CallIntMethod(exception, KKJE.resultCode);

            IfExceptionThrown {
                rc = SQLITE_ERROR;
            }
        }

        LocalRefDestroy(exception);
    }

    return rc;
}

/**
 * Calls the Java auto_vacuum_pages hook.
 */
static unsigned int autoVacuumPagesCaller(
    void* pDbStateHook,
    const char* zSchema,
    unsigned int nDbPage,
    unsigned int nFreePage,
    unsigned int nBytePerPage
) {
    JniEnvDeclare();
    DbStateDeclare(pDbStateHook);

    const auto schema = Utf8ToJstring(zSchema);

    HookEnterDbState(autoVacuumPages);
    uint result = env->CallIntMethod(instance, call, schema, nDbPage, nFreePage, nBytePerPage);
    HookLeave();
    LocalRefDestroy(schema);

    IfExceptionThrown {
        result = nFreePage;
    }

    return result;
}

/**
 * Calls the Java busy_handler hook.
 */
static int busyHandlerCaller(
    void* pDbStateHook,
    int n
) {
    JniEnvDeclare();
    DbStateDeclare(pDbStateHook);

    HookEnterDbState(busyHandler);
    jint result = env->CallIntMethod(instance, call, n);
    HookLeave();

    IfExceptionThrown {
        result = 0;
    }

    return result;
}

/**
 * Calls the Java collation_needed hook.
 */
static void collationNeededCaller(
    void* pDbStateHook,
    sqlite3* pDb,
    int eTextRep,
    const char* zName
) {
    JniEnvDeclare();
    DbStateDeclare(pDbStateHook);

    const auto db = PtrToLong(pDb);
    const auto name = Utf8ToJstring(zName);

    HookEnterDbState(collationNeeded);
    env->CallVoidMethod(instance, call, db, eTextRep, zName);
    HookLeave();
    LocalRefDestroy(name);
}

///////////////////////////////////////////////////////////////////////////
// Ksqlite + SQLite 1 to 1 mapping
///////////////////////////////////////////////////////////////////////////

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1auto_1extension(
    JNIEnv* env,
    jclass clazz,
    jobject callback
) {
    if (callback == nullptr) {
        return SQLITE_MISUSE;
    }

    auto rc = SQLITE_OK;

    MutexEnter(KHS);
    auto& hook = KHS.autoExtension;

    if (hook.instance != nullptr) {
        if (!env->IsSameObject(callback, hook.instance)) {
            rc = SQLITE_MISUSE;
        }
    } else {
        rc = ksqlite_auto_extension(autoExtensionCaller);

        if (rc == SQLITE_OK) {
            HookConfigure(hook, callback, "(JJ)I", AUTO_EXTENSION_CALLBACK);
        }
    }

    MutexLeave(KHS);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1cancel_1auto_1extension(
    JNIEnv* env,
    jclass clazz,
    jobject callback
) {
    auto rc = SQLITE_OK;

    MutexEnter(KHS);
    auto& hook = KHS.autoExtension;

    if (hook.instance != nullptr && env->IsSameObject(callback, hook.instance)) {
        rc = ksqlite_cancel_auto_extension(autoExtensionCaller);

        if (rc == SQLITE_OK) {
            HookClear(hook);
        }
    }

    MutexLeave(KHS);
    return rc;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1aggregate_1context(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint nBytes
) {
    const auto s3Context = LongTo_s3_context(context);
    const auto pointer = sqlite3_aggregate_context(s3Context, nBytes);

    return PtrToLong(pointer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1autovacuum_1pages(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback,
    jobject destructor
) {
    const auto pDb = LongTo_s3(db);

    // Force previous callback destructor invocation
    auto rc = sqlite3_autovacuum_pages(pDb, nullptr, nullptr, nullptr);

    if (rc != SQLITE_OK) {
        return rc;
    }

    DbStateMutexEnter(pDb);
    const auto pHook = &dbState.hooks.autoVacuumPages;
    auto& hook = *pHook;

    // Ensure that destructor, if any, has been called
    RequireNull(hook.instance);
    RequireNull(hook.destructor);

    if (callback != nullptr) {
        rc = sqlite3_autovacuum_pages(pDb, autoVacuumPagesCaller, pDbState, hookDestroyer);

        if (rc == SQLITE_OK) {
            HookDestroyableConfigure(
                hook,
                destructor,
                callback,
                "(Ljava/lang/String;III)I",
                AUTO_VACUUM_PAGES_CALLBACK
            );
        }
    } else {
        // For now, forbid setting a destructor without a callback
        RequireNull(destructor);
    }

    DbStateMutexLeave();
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1finish(
    JNIEnv* env,
    jclass clazz,
    jlong backup
) {
    return sqlite3_backup_finish(LongTo_s3_backup(backup));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1init(
    JNIEnv* env,
    jclass clazz,
    jlong destDb,
    jstring destDbName,
    jlong srcDb,
    jstring srcDbName
) {
    const auto pDest = LongTo_s3(destDb);
    const auto pSource = LongTo_s3(srcDb);
    const auto zDestName = JstringToUtf8(destDbName);
    const auto zSourceName = JstringToUtf8(srcDbName);
    const auto backupPtr = sqlite3_backup_init(pDest, zDestName, pSource, zSourceName);

    sqlite3_free(zDestName);
    sqlite3_free(zSourceName);

    return PtrToLong(backupPtr);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1pagecount(
    JNIEnv* env,
    jclass clazz,
    jlong backup
) {
    return sqlite3_backup_pagecount(LongTo_s3_backup(backup));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1remaining(
    JNIEnv* env,
    jclass clazz,
    jlong backup
) {
    return sqlite3_backup_remaining(LongTo_s3_backup(backup));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1step(
    JNIEnv* env,
    jclass clazz,
    jlong backup,
    jint nPage
) {
    return sqlite3_backup_step(LongTo_s3_backup(backup), nPage);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1blob(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jbyteArray data,
    jint size,
    jobject destructor
) {
    DestructorCheck(data, SQLITE_MISUSE)

    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto buffer = ByteArrayToBuffer(data, size);
    const auto freeable = AllocateFreeablePointer(buffer, destructor);
    const auto destroyer = FreeableDestroyerPush(buffer, freeable);

    return sqlite3_bind_blob(pStmt, index, buffer, size, destroyer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1blob64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject data,
    jlong size,
    jobject destructor
) {
    DestructorCheck(data, SQLITE_MISUSE)

    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto buffer = BufferDirectAddress(data);
    const auto freeable = AllocateFreeableTarget(data, destructor);
    const auto destroyer = FreeableDestroyerPush(buffer, freeable);

    return sqlite3_bind_blob64(pStmt, index, buffer, size, destroyer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1double(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jdouble value
) {
    return sqlite3_bind_double(LongTo_s3_stmt(stmt), index, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1int(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jint value
) {
    return sqlite3_bind_int(LongTo_s3_stmt(stmt), index, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1int64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong value
) {
    return sqlite3_bind_int64(LongTo_s3_stmt(stmt), index, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1null(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_bind_null(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1parameter_1count(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_bind_parameter_count(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1parameter_1index(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jstring name
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto zName = JstringToUtf8(name);
    const auto index = sqlite3_bind_parameter_index(pStmt, zName);

    sqlite3_free(zName);
    return index;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1parameter_1name(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_bind_parameter_name(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1pointer(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject data,
    jstring type,
    jobject destructor
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto zType = JstringToUtf8(type);
    const auto freeable = AllocateFreeable(zType, data, destructor);
    const auto destroyer = FreeableDestroyer(freeable);

    return sqlite3_bind_pointer(pStmt, index, freeable, zType, destroyer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1text(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jstring text,
    jint size,
    jboolean computeSize
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    size_t textSize = 0;
    const auto buffer = JstringToUtf8Out(text, &textSize);
    const auto freeable = AllocateFreeablePointer(buffer, nullptr);
    const auto pDestructor = FreeableDestroyerPush(buffer, freeable);

    if (computeSize == JNI_TRUE) {
        size = static_cast<jint>(textSize);
    }

    return sqlite3_bind_text(pStmt, index, buffer, size, pDestructor);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1text64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject data,
    jlong size,
    jobject destructor,
    jint encoding
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto buffer = reinterpret_cast<char*>(BufferDirectAddress(data));
    const auto freeable = AllocateFreeableTarget(data, destructor);
    const auto destroyer = FreeableDestroyerPush(buffer, freeable);

    return sqlite3_bind_text64(pStmt, index, buffer, size, destroyer, encoding);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1value(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong value
) {
    return sqlite3_bind_value(LongTo_s3_stmt(stmt), index, LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1zeroblob(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jint size
) {
    return sqlite3_bind_zeroblob(LongTo_s3_stmt(stmt), index, size);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1zeroblob64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong size
) {
    return sqlite3_bind_zeroblob64(LongTo_s3_stmt(stmt), index, size);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1blob_1bytes(
    JNIEnv* env,
    jclass clazz,
    jlong blob
) {
    return sqlite3_blob_bytes(LongTo_s3_blob(blob));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1blob_1close(
    JNIEnv* env,
    jclass clazz,
    jlong blob
) {
    return sqlite3_blob_close(LongTo_s3_blob(blob));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1blob_1open(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring databaseName,
    jstring tableName,
    jstring columnName,
    jlong rowIndex,
    jint flags,
    jobject outBlob
) {
    const auto pDb = LongTo_s3(db);
    const auto zDb = JstringToUtf8(databaseName);
    const auto zTable = JstringToUtf8(tableName);
    const auto zColumn = JstringToUtf8(columnName);
    auto pBlob = OutputPointerGetPointerValue(sqlite3_blob, outBlob);
    const auto rc = sqlite3_blob_open(pDb, zDb, zTable, zColumn, rowIndex, flags, &pBlob);

    sqlite3_free(zDb);
    sqlite3_free(zTable);
    sqlite3_free(zColumn);

    if (rc == SQLITE_OK) {
        OutputPointerSetPointerValue(outBlob, pBlob);
    }

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1blob_1read(
    JNIEnv* env,
    jclass clazz,
    jlong blob,
    jbyteArray buffer,
    jint size,
    jint offset
) {
    const auto pBlob = LongTo_s3_blob(blob);
    const auto elements = env->GetByteArrayElements(buffer);
    OutOfMemoryCheck(elements);

    const auto rc = sqlite3_blob_read(pBlob, elements, size, offset);

    if (rc == SQLITE_OK) {
        env->ReleaseByteArrayElements(buffer, elements, JNI_COMMIT);
    } else {
        env->ReleaseByteArrayElements(buffer, elements, JNI_ABORT);
    }

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1blob_1reopen(
    JNIEnv* env,
    jclass clazz,
    jlong blob,
    jlong rowIndex
) {
    return sqlite3_blob_reopen(LongTo_s3_blob(blob), rowIndex);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1blob_1write(
    JNIEnv* env,
    jclass clazz,
    jlong blob,
    jbyteArray buffer,
    jint size,
    jint offset
) {
    const auto pBlob = LongTo_s3_blob(blob);
    const auto pBuffer = ByteArrayToBuffer(buffer, size);
    const auto rc = sqlite3_blob_write(pBlob, pBuffer, size, offset);
    sqlite3_free(pBuffer);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1busy_1handler(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplace(
        busyHandler,
        "(I)I",
        BUSY_HANDLER_CALLBACK,
        sqlite3_busy_handler(pDb, busyHandlerCaller, pDbState)
    );
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1busy_1timeout(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint millis
) {
    return sqlite3_busy_timeout(LongTo_s3(db), millis);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1changes(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_changes(LongTo_s3(db));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1changes64(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_changes64(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1clear_1bindings(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_clear_bindings(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1close(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_close(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1close_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_close_v2(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1collation_1needed(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplace(
        busyHandler,
        "(JILjava/lang/String;)V",
        COLLATION_NEEDED_CALLBACK,
        sqlite3_collation_needed(pDb, pDbState, collationNeededCaller)
    );
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1blob(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto pBlob = sqlite3_column_blob(pStmt, index);

    if (pBlob == nullptr) {
        return nullptr;
    }

    const auto length = sqlite3_column_bytes(pStmt, index);

    if (length == 0) {
        return env->NewByteArray(0);
    }

    switch (sqlite3_column_type(pStmt, index)) {
        case SQLITE_NULL:
            return nullptr;
        case SQLITE_BLOB:
            return BufferToByteArray(pBlob, length);
        default:
            env->ThrowNew(KJV.illegalStateException, "Column is not a blob");
            return nullptr;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1bytes(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_column_bytes(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1count(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_column_count(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1database_1name(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_column_database_name(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1decltype(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_column_decltype(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1double(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_column_double(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1int(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_column_int(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1int64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_column_int64(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1name(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_column_name(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1origin_1name(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_column_origin_name(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1table_1name(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_column_table_name(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1text(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(
        reinterpret_cast<const char*>(sqlite3_column_text(LongTo_s3_stmt(stmt), index))
    );
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1type(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_column_type(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1value(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return PtrToLong(sqlite3_column_value(LongTo_s3_stmt(stmt), index));
}