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

#define CONCAT(A, B) A##B
#define UNDERSCORED(V) CONCAT(V, _)

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

#define RequireClass(className) \
    getClassOrDie(env, className, "Error getting reference to " className " class")

#define RequireKsqliteClass(className) RequireClass("ksqlite/" className)

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

#define MethodStringCall(instance, methodId, ...) \
    reinterpret_cast<jstring>(env->CallObjectMethod(instance, methodId __VA_OPT__(,) __VA_ARGS__))

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

#define RequireKsqliteClassField(klass, name, signature, className) \
    RequireClassField(klass, name, signature, "ksqlite." className)

#define RequireKsqliteField(O, name, signature, className) \
    RequireKsqliteClassField(O.klass, name, signature, className)

/**
 * Ensures that an object is an instance of a given type.
 */
#define RequireObjectIsInstance(instance, klass, message, ...) \
    if (!env->IsInstanceOf(instance, klass))                   \
        FatalError(sqlite3_mprintf(message __VA_OPT__(,) __VA_ARGS__))

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
    jmethodID destroy;
};

/**
 * Clears a Destroyable.
 */
static inline void clearDestroyable(
    JNIEnv* env,
    Destroyable* pDestroyable
) {
    GlobalRefDestroy(pDestroyable->destructor);
    pDestroyable->destructor = nullptr;
    pDestroyable->destroy = nullptr;
}

#define DestroyableClear(destroyable) clearDestroyable(env, &destroyable)

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
    // Mutex to hold while reading hook variables
    MutexGuarded* pGuard;
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
#define HookDestroyableConfigure(hook, jDestructor, instance, signature, className) \
    if (destructor != nullptr) hook.destructor = GlobalRefCreate(jDestructor); \
    hook.destroy = KKDC.destroy; \
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
    Hook* pHook
) {
    GlobalRefDestroy(pHook->instance);
    pHook->instance = nullptr;
    pHook->call = nullptr;
}

#define HookClear(hook) clearHook(env, &hook)

/**
 * Clears a hook and invokes its Java destructor.
 */
static void destroyHook(
    JNIEnv* env,
    void* pHook
) {
    const auto hookPtr = reinterpret_cast<HookDestroyable*>(pHook);
    auto& hook = *hookPtr;
    const auto pGuard = hook.pGuard;

    if (pGuard != nullptr) {
        sqlite3_mutex_enter(pGuard->mutex);
    }

    if (hook.instance != nullptr) {
        HookClear(hook);
    }

    jobject destructor = nullptr;
    jmethodID destroy = nullptr;

    if (hook.destructor != nullptr) {
        destructor = LocalRefCreate(RequireNonNullJobject(hook.destructor));
        destroy = hook.destroy;
        DestroyableClear(hook);
    }

    if (pGuard != nullptr) {
        sqlite3_mutex_leave(pGuard->mutex);
    }

    if (destructor != nullptr) {
        env->CallVoidMethod(destructor, destroy);
        LocalRefDestroy(destructor);
    }
}

///////////////////////////////////////////////////////////////////////////
// Freeable
///////////////////////////////////////////////////////////////////////////

/**
 * Holder for an optional pointer allocated with sqlite3_malloc(), an optional Java object to keep
 * globally reachable and an optional globally referenced destructor.
 */
struct Freeable : Destroyable {
    void* pointer;
    jobject data;
};

typedef std::unordered_map<void*, Freeable*> FreeableMap;

/**
 * Allocates a new Freeable if at least one of pointer, data or destructor is not null. Returns
 * null if none of the supplied arguments is not null.
 */
static Freeable* allocateFreeable(
    JNIEnv* env,
    void* pointer,
    jobject data,
    jobject destructor,
    jmethodID destroy
) {
    if (pointer == nullptr && data == nullptr && destructor == nullptr) {
        return nullptr;
    }

    jobject globalData = nullptr;
    jobject globalDestructor = nullptr;

    if (data != nullptr) {
        globalData = GlobalRefCreate(data);
    }

    if (destructor != nullptr) {
        globalDestructor = GlobalRefCreate(destructor);
    }

    return new Freeable { { globalDestructor, destroy }, pointer, globalData };
}

/**
 * Calls the Java destructor for the given Freeable and releases associated resources.
 * The pointer must have been allocated with `new`.
 */
static void destroyFreeable(
    JNIEnv* env,
    void* pFreeable
) {
    if (pFreeable == nullptr) {
        return;
    }

    const auto freeablePtr = reinterpret_cast<Freeable*>(pFreeable);
    auto& freeable = *freeablePtr;

    jobject destructor = LocalRefCreate(freeable.destructor);

    if (destructor != nullptr) {
        GlobalRefDestroy(freeable.destructor);
        env->CallVoidMethod(destructor, freeable.destroy);
        LocalRefDestroy(destructor);
    }

    if (freeable.data != nullptr) {
        GlobalRefDestroy(freeable.data);
    }

    if (freeable.pointer != nullptr) {
        sqlite3_free(freeable.pointer);
    }

    delete freeablePtr;
}

#define AllocateFreeable(pointer, data, destructor) \
    allocateFreeable(env, pointer, data, destructor, KKDC.destroy)

#define AllocateFreeablePointer(pointer, destructor) \
    AllocateFreeable(pointer, nullptr, destructor)

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * Holder for scalar and aggregate function hooks.
 */
struct Function : Destroyable {
    jobject appData;
    Hook func;
    Hook step;
    Hook final;
};

/**
 * Holder for window function hooks.
 */
struct FunctionWindow : Function {
    Hook inverse;
    Hook value;
};

/**
 * Initializes the given Function.
 */
static void initializeFunction(
    JNIEnv* env,
    Function& function,
    jobject appData,
    jobject destructor,
    jmethodID destroy
) {
    if (appData != nullptr) {
        function.appData = GlobalRefCreate(appData);
    }

    if (destructor != nullptr) {
        function.destructor = GlobalRefCreate(destructor);
        function.destroy = destroy;
    }
}

/**
 * Allocates and returns a new Function.
 */
static Function* allocateFunction(
    JNIEnv* env,
    jobject appData,
    jobject destructor,
    jmethodID destroy
) {
    Function function { };
    initializeFunction(env, function, appData, destructor, destroy);
    return new Function(function);
}

/**
 * Allocates and returns a new Function.
 */
static FunctionWindow* allocateFunctionWindow(
    JNIEnv* env,
    jobject appData,
    jobject destructor,
    jmethodID destroy
) {
    FunctionWindow function { };
    initializeFunction(env, function, appData, destructor, destroy);
    return new FunctionWindow(function);
}

/**
 * Clears the given function and invokes the destructor if any.
 */
static void clearFunction(
    JNIEnv* env,
    Function& function
) {
    if (function.appData != nullptr) {
        GlobalRefDestroy(function.appData);
    }

    if (function.func.instance != nullptr) {
        HookClear(function.func);
    }

    if (function.step.instance != nullptr) {
        HookClear(function.step);
    }

    if (function.final.instance != nullptr) {
        HookClear(function.final);
    }

    if (function.destructor != nullptr) {
        env->CallVoidMethod(function.destructor, function.destroy);
        DestroyableClear(function);
    }
}

/**
 * Destroys the given function and invokes the destructor if any.
 */
static void destroyFunction(
    JNIEnv* env,
    Function* pFunction
) {
    auto& function = *pFunction;
    clearFunction(env, function);
    delete pFunction;
}

/**
 * Destroys the given window function and invokes the destructor if any.
 */
static void destroyFunctionWindow(
    JNIEnv* env,
    FunctionWindow* pFunction
) {
    auto& function = *pFunction;

    if (function.inverse.instance != nullptr) {
        HookClear(function.inverse);
    }

    if (function.value.instance != nullptr) {
        HookClear(function.value);
    }

    clearFunction(env, function);
    delete pFunction;
}

///////////////////////////////////////////////////////////////////////////
// Database
///////////////////////////////////////////////////////////////////////////

/**
 * Database connection state.
 */
struct DbState : MutexGuarded {

    char* configMainDbName; // SQLITE_DBCONFIG_MAINDBNAME, must be freed with sqlite3_free()

    struct {
        Hook authorizer;
        HookDestroyable autoVacuumPages;
        Hook busyHandler;
        HookDestroyable collationCompare;
        Hook collationNeeded;
        Hook commitHook;
        Hook preupdateHook;
        Hook progressHandler;
        Hook rollbackHook;
        Hook trace;
        Hook updateHook;
        Hook walHook;
    } hooks;
};

typedef std::unordered_map<sqlite3*, DbState*> DbStateMap;

/**
 * Allocates and returns a new DbState*.
 */
static DbState* allocateDbState(JNIEnv* env) {
    auto state = DbState();
    const auto pDbState = new DbState(state);
    MutexAllocate(state);

    return pDbState;
}

/**
 * Destroys the state of a closed database.
 */
static void destroyDbState(
    JNIEnv* env,
    DbState* pState
) {
    // Cleanup the database state
    auto& state = *pState;
    auto& hooks = state.hooks;

    // Clear hooks to release Java references.
    HookClear(hooks.authorizer);
    HookClear(hooks.busyHandler);
    HookClear(hooks.collationNeeded);
    HookClear(hooks.commitHook);
    HookClear(hooks.preupdateHook);
    HookClear(hooks.progressHandler);
    HookClear(hooks.rollbackHook);
    HookClear(hooks.trace);
    HookClear(hooks.updateHook);
    HookClear(hooks.walHook);

    // Destructors must have been called by SQLite for theses hooks.
    RequireNull(hooks.autoVacuumPages.instance);
    RequireNull(hooks.collationCompare.instance);

    sqlite3_free(state.configMainDbName);
    MutexDestroy(state);
    delete pState;
}

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

    // Global hooks
    struct : MutexGuarded {
        Hook autoExtension;
        Hook log;
        Hook sqlLog;
    } hooks;

    // Java cache
    struct {
        jbyteArray emptyByteArray;
        jclass byteBuffer;
        jclass illegalArgumentException;
        jclass illegalStateException;
        jclass string;

        struct : Class {
            jmethodID getName; // ()Ljava/lang/String;
        } jClass; // Class

        struct : Class {
            jmethodID constructor; // (I)V
            jmethodID intValue; // ()I
        } int32; // Integer

        struct : Class {
            jmethodID constructor; //(J)V
            jmethodID longValue; // ()J
        } int64; // Long
    } java;

    // Ksqlite cache
    struct {
        void* emptyBufferPointer;
        jclass configLogCallback;
        jclass configSqlLogCallback;

        struct : Class {
            jmethodID destroy; // ()V
        } destructorCallback;

        struct : Class {
            jmethodID call; // (I[Ljava/lang/String;[Ljava/lang/String;)I
        } execCallback;

        struct : Class {
            jmethodID resultCode; // ()I
            jmethodID message; // ()Ljava/lang/String;
        } jniException;

        struct : Class {
            jfieldID value; // Ljava/lang/Object;
        } outputPointer;
    } ksqlite;
} KsqliteJniGlobalState;

#define K KsqliteJniGlobalState
#define KDS K.dbs
#define KFS K.freeables
#define KHS K.hooks

#define KJV K.java
#define KJVC K.java.jClass
#define KJVI K.java.int32
#define KJVL K.java.int64

#define KK K.ksqlite
#define KKDC K.ksqlite.destructorCallback
#define KKEC K.ksqlite.execCallback
#define KKJE K.ksqlite.jniException
#define KKOP K.ksqlite.outputPointer
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

#define JAVA_CLASS "java.lang.Class"
#define JAVA_INT "java.lang.Integer"
#define JAVA_LONG "java.lang.Long"

/**
 * Initializes and caches the Java related classes and objects.
 */
static void initializeJavaJniCache(JNIEnv* env) {
    const auto byteArray = env->NewByteArray(0);
    OutOfMemoryCheck(KJV.emptyByteArray != nullptr);
    KJV.emptyByteArray = reinterpret_cast<jbyteArray>(GlobalRefCreate(byteArray));

    // Classes only
    KJV.byteBuffer = RequireClass("java/nio/ByteBuffer");
    KJV.illegalArgumentException = RequireClass("java/lang/IllegalArgumentException");
    KJV.illegalStateException = RequireClass("java/lang/IllegalStateException");
    KJV.string = RequireClass("java/lang/String");

    // Class
    KJVC.klass = RequireClass("java/lang/Class");
    KJVC.getName = RequireMethod(KJVC, "getName", "()Ljava/lang/String;", JAVA_CLASS);

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

    ClassClear(KJVC);
    KJVC.getName = nullptr;

    GlobalRefDestroy(KJV.byteBuffer);
    GlobalRefDestroy(KJV.illegalStateException);
    GlobalRefDestroy(KJV.illegalArgumentException);
    GlobalRefDestroy(KJV.string);
    GlobalRefDestroy(KJV.emptyByteArray);
}

#define KSQLITE_JNI_EXCEPTION "KsqliteJniException"
#define DESTRUCTOR_CALLBACK "DestructorCallback"
#define EXEC_CALLBACK "ExecCallback"
#define OUTPUT_POINTER "OutputPointer"

/**
 * Initializes and caches the Ksqlite related classes and objects.
 */
static void initializeKsqliteJniCache(JNIEnv* env) {
    KK.emptyBufferPointer = sqlite3_malloc(sizeof(void*));
    OutOfMemoryCheck(KK.emptyBufferPointer != nullptr);

    // Classes only
    KK.configLogCallback = RequireKsqliteClass("ConfigLogCallback");
    KK.configSqlLogCallback = RequireKsqliteClass("ConfigSqlLogCallback");

    // DestructorCallback
    KKDC.klass = RequireKsqliteClass(DESTRUCTOR_CALLBACK);
    KKDC.destroy = RequireKsqliteMethod(KKDC, "destroy", "()V", DESTRUCTOR_CALLBACK);

    // ExecCallback
    KKEC.klass = RequireKsqliteClass(EXEC_CALLBACK);

    KKEC.call = RequireKsqliteMethod(KKEC,
        "call",
        "(I[Ljava/lang/String;[Ljava/lang/String;)I",
        EXEC_CALLBACK
    );

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

    ClassClear(KKEC);
    KKEC.call = nullptr;

    ClassClear(KKDC);
    KKDC.destroy = nullptr;

    GlobalRefDestroy(KK.configLogCallback);
    GlobalRefDestroy(KK.configSqlLogCallback);

    sqlite3_free(KK.emptyBufferPointer);
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
// Casting
///////////////////////////////////////////////////////////////////////////

#define PtrToLong(P) reinterpret_cast<jlong>(P)
#define LongToPtr(L) reinterpret_cast<void*>(L)

#define LongCast(T, L) reinterpret_cast<T*>(L)
#define LongTo_s3(L) LongCast(sqlite3, (L))
#define LongTo_s3_backup(L) LongCast(sqlite3_backup, (L))
#define LongTo_s3_blob(L) LongCast(sqlite3_blob, (L))
#define LongTo_s3_context(L) LongCast(sqlite3_context, (L))
#define LongTo_s3_index_info(L) LongCast(sqlite3_index_info, (L))
#define LongTo_s3_snapshot(L) LongCast(sqlite3_snapshot, (L))
#define LongTo_s3_stmt(L) LongCast(sqlite3_stmt, (L))
#define LongTo_s3_value(L) LongCast(sqlite3_value, (L))
#define LongTo_s3_vfs(L) LongCast(sqlite3_vfs, (L))

///////////////////////////////////////////////////////////////////////////
// Hook operations
///////////////////////////////////////////////////////////////////////////

/**
 * Declares the code needed to replace a global hook.
 */
#define GlobalHookReplace(H, function, initValue, configure, result) \
    auto R = initValue;                                              \
                                                                     \
    MutexEnter(K.hooks);                                             \
    const auto pHook = &K.hooks.H;                                   \
    auto& hook = *pHook;                                             \
                                                                     \
    if (hook.instance != nullptr) {                                  \
        HookClear(hook);                                             \
    }                                                                \
                                                                     \
    if (callback != nullptr) {                                       \
        R = function;                                                \
        configure                                                    \
    }                                                                \
                                                                     \
    MutexLeave(K.hooks);                                             \
    return result

/**
 * Declares the code needed to replace a database connection hook without destructor.
 * The function is expected to return an sqlite result code.
 */
#define GlobalHookReplaceRC(H, S, N, F) \
    GlobalHookReplace(H, F, SQLITE_OK, if (R == SQLITE_OK) { HookConfigure(hook, callback, S, N); }, R)

/**
 * Calls the Java destructor for the given Hook pointer and releases associated resources.
 */
static void hookDestroyer(void* pHook) {
    destroyHook(retrieveJniEnv(), pHook);
}

///////////////////////////////////////////////////////////////////////////
// Function operations
///////////////////////////////////////////////////////////////////////////

#define FunctionHookConfigure(hook, object, signature, className) \
    { HookConfigure(function.hook, object, signature, "FunctionCallback." className); }

#define Function1HookConfigure(hook, object, className) \
    FunctionHookConfigure(hook, object, "(J)V", className)

#define Function2HookConfigure(hook, object, className) \
    FunctionHookConfigure(hook, object, "(J[J)V", className)

#define FunctionFuncHookConfigure(object) Function2HookConfigure(func, object, "Func")
#define FunctionStepHookConfigure(object) Function2HookConfigure(step, object, "Step")
#define FunctionFinalHookConfigure(object) Function1HookConfigure(final, object, "Final")
#define FunctionInverseHookConfigure(object) Function2HookConfigure(inverse, object, "Inverse")
#define FunctionValueHookConfigure(object) Function1HookConfigure(value, object, "Value")

/**
 * Declares the function pointer P as T.
 */
#define FunctionPointerDeclare(T, P) \
    const auto pFunction = reinterpret_cast<T*>(P)

/**
 * Declares the variables for a function.
 */
#define FunctionDeclare(T, P) \
    FunctionPointerDeclare(T, P); \
    auto& function = *pFunction

/**
 * Declares the variables for a function in a callback from the pContext parameter.
 */
#define FunctionDeclareFromContext(T) FunctionDeclare(T, sqlite3_user_data(pContext))

/**
 * Declares the variables for a function and a hook in a callback.
 */
#define FunctionDeclareHook(T, H) \
    FunctionDeclareFromContext(T); \
    auto hook = function.H

#define FunctionHookEnter() \
    const auto instance = LocalRefCreate(RequireNonNullJobject(hook.instance)); \
    const auto call = hook.call

#define FunctionHookLeave() LocalRefDestroy(instance)

/**
 * Calls an instance of `FunctionCallback.Func1` hook.
 */
static void callFunctionFunc1(
    Hook& hook,
    sqlite3_context* pContext
) {
    JniEnvDeclare();
    const auto context = PtrToLong(pContext);

    FunctionHookEnter();
    env->CallVoidMethod(instance, call, context);
    FunctionHookLeave();
}

/**
 * Calls an instance of `FunctionCallback.Func2` hook.
 */
static void callFunctionFunc2(
    Hook& hook,
    sqlite3_context* pContext,
    int argc,
    sqlite3_value** argv
) {
    JniEnvDeclare();

    const auto context = PtrToLong(pContext);
    const auto longArray = env->NewLongArray(argc);
    OutOfMemoryCheck(longArray != nullptr);

    constexpr auto maxStackArgs = 32;
    jlong stackBuffer[maxStackArgs];
    jlong* buffer = stackBuffer;

    if (argc > maxStackArgs) {
        buffer = new jlong[argc];
    }

    for (int i = 0; i < argc; ++i) {
        buffer[i] = reinterpret_cast<jlong>(argv[i]);
    }

    FunctionHookEnter();
    env->CallVoidMethod(instance, call, context, longArray);
    FunctionHookLeave();

    if (buffer != stackBuffer) {
        delete[] buffer;
    }

    env->DeleteLocalRef(longArray);
}

/**
 * Destroys an aggregate or scalar function.
 */
static void functionDestroyer(void* pAppData) {
    JniEnvDeclare();
    FunctionPointerDeclare(Function, pAppData);
    destroyFunction(env, pFunction);
}

/**
 * Destroys a window function.
 */
static void functionWindowDestroyer(void* pAppData) {
    JniEnvDeclare();
    FunctionPointerDeclare(FunctionWindow, pAppData);
    destroyFunctionWindow(env, pFunction);
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
    sqlite3* pDb
) {
    RequireNonNull(pDb);
    const auto pDbState = allocateDbState(env);

    MutexEnter(KDS);
    const auto [iterator, inserted] = KDS.map->emplace(pDb, pDbState);
    MutexLeave(KDS);

    if (!inserted) {
        FatalError("Database state is already initialized");
    }

    return iterator->second;
}

/**
 * Removes and destroys the state of a closed database connection.
 */
static void popDbState(
    JNIEnv* env,
    sqlite3* pDb
) {
    RequireNonNull(pDb);
    const auto& map = KDS.map;

    MutexEnter(KDS);
    const auto iterator = map->find(pDb);

    if (iterator == map->end()) {
        DbStateNotFoundFatalError();
    }

    const auto pDbState = iterator->second;
    map->erase(iterator);
    MutexLeave(KDS);

    destroyDbState(env, pDbState);
}

/**
 * Returns the state of the supplied database.
 */
static DbState* getDbState(
    JNIEnv* env,
    sqlite3* pDb
) {
    if (pDb == nullptr) {
        return nullptr;
    }

    const auto& map = KDS.map;

    MutexEnter(KDS);
    const auto iterator = KDS.map->find(pDb);

    if (iterator == map->end()) {
        DbStateNotFoundFatalError();
    }

    const auto pDbState = iterator->second;
    MutexLeave(KDS);

    return pDbState;
}

#define DbStateGet(db) getDbState(env, db)

/**
 * Declares the variables to the database state in a hook caller function with P as direct pointer
 * to DbState.
 */
#define DbStateDeclareDirect(P) \
    const auto pDbState = reinterpret_cast<DbState*>(P); \
    auto& dbState = *pDbState \

/**
 * Declares the variables to the database state in a hook caller function with P a pointer to
 * HookDestroyable.
 */
#define DbStateDeclareHook(P) \
    const auto pHookDestroyable = reinterpret_cast<HookDestroyable*>(P); \
    DbStateDeclareDirect(pHookDestroyable->pGuard)

/**
 * Returns the state of the supplied database and enter its mutex.
 */
#define DbStateMutexEnter(db) \
    const auto pDbState = DbStateGet(db); \
    auto& dbState = *pDbState; \
    MutexEnter(dbState)

/**
 * Leave the mutex from a previous call to DbStateMutexEnter().
 */
#define DbStateMutexLeave() MutexLeave(dbState)

/**
 * Declares the code needed to replace a database connection hook with a destructor.
 * For now setting a destructor with no callback returns SQLITE_MISUSE.
 *
 * The hook's mutex is disabled during the function call to prevent dead lock as the function call
 * may invoke the destructor.
 */
#define DbHookDestructorReplace(H, signature, className, install, uninstall, ...)       \
    if (callback == nullptr && destructor != nullptr) {                                 \
        return SQLITE_MISUSE;                                                           \
    }                                                                                   \
                                                                                        \
    const auto pDb = LongTo_s3(db);                                                     \
    auto rc = SQLITE_OK;                                                                \
                                                                                        \
    DbStateMutexEnter(pDb);                                                             \
    const auto pHook = &dbState.hooks.H;                                                \
    auto& hook = *pHook;                                                                \
    hook.pGuard = nullptr;                                                              \
                                                                                        \
    if (callback != nullptr) {                                                          \
        rc = install;                                                                   \
                                                                                        \
        if (rc == SQLITE_OK) {                                                          \
            HookDestroyableConfigure(hook, destructor, callback, signature, className); \
        }                                                                               \
    } else if (hook.instance != nullptr) {                                              \
        uninstall;                                                                      \
    }                                                                                   \
                                                                                        \
    hook.pGuard = pDbState;                                                             \
    DbStateMutexLeave();                                                                \
    __VA_ARGS__                                                                         \
    return rc

/**
 * Declares the code needed to replace a database connection hook without destructor.
 */
#define DbHookReplace(H, resultDeclare, configure, install, uninstall, resultReturn)    \
    const auto pDb = LongTo_s3(db);                                                     \
                                                                                        \
    DbStateMutexEnter(pDb);                                                             \
    const auto pHook = &dbState.hooks.H;                                                \
    auto& hook = *pHook;                                                                \
    const auto oldCallback = LocalRefCreate(hook.instance);                             \
    resultDeclare                                                                       \
                                                                                        \
    if (callback != nullptr) {                                                          \
        install                                                                         \
        configure                                                                       \
    } else if (oldCallback != nullptr) {                                                \
        uninstall                                                                       \
        HookClear(hook);                                                                \
    }                                                                                   \
                                                                                        \
    DbStateMutexLeave();                                                                \
    resultReturn

/**
 * Declares the code needed to replace a database connection hook without destructor.
 * The function is expected to return an sqlite result code.
 */
#define DbHookReplaceResultCode(H, signature, className, install, uninstall) DbHookReplace(H,   \
    auto rc = SQLITE_OK;,                                                                       \
    if (rc == SQLITE_OK) { HookConfigure(hook, callback, signature, className); },              \
    rc = install;,                                                                              \
    rc = uninstall;,                                                                            \
    return rc                                                                                   \
)

/**
 * Declares the code needed to replace a database connection hook without destructor.
 * The previous instance is returned.
 */
#define DbHookReplaceInstance(H, signature, className, install, uninstall) DbHookReplace(H,,    \
    install;, uninstall;,                                                                       \
    HookConfigure(hook, callback, signature, className);,                                       \
    return oldCallback                                                                          \
)

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

/**
 * Calls the Java destructor for the given Freeable pointer and releases associated resources.
 * The pointer must have been allocated with `new`.
 */
static void freeableDestroyer(void* pFreeable) {
    destroyFreeable(retrieveJniEnv(), pFreeable);
}

/**
 * Retrieves the Freeable pointer associated with pointer and calls the Java destructor releasing
 * associated resources.
 */
static void freeableDestroyerPop(void* pointer) {
    const auto env = retrieveJniEnv();
    const auto pFreeable = FreeablePop(pointer);
    destroyFreeable(env, pFreeable);
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
    RequireNonNull(key);

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

///////////////////////////////////////////////////////////////////////////
// Blob helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Declares the common logic for blob (column_blob, column_buffer, value_blob, value_buffer).
 */
#define BufferBlobDeclare(T, emptyBlob, toBlob, getPointer, getLength, getType, ...)        \
    const auto pointer = getPointer;                                                        \
    T result = nullptr;                                                                     \
                                                                                            \
    if (pointer != nullptr) {                                                               \
        const auto length = getLength;                                                      \
                                                                                            \
        if (length == 0) {                                                                  \
            result = emptyBlob;                                                             \
        } else {                                                                            \
            switch (const auto type = getType) {                                            \
                case SQLITE_NULL:                                                           \
                    result = nullptr;                                                       \
                    break;                                                                  \
                case SQLITE_BLOB:                                                           \
                    result = toBlob;                                                        \
                    break;                                                                  \
                default:                                                                    \
                    env->ThrowNew(                                                          \
                        KJV.illegalStateException,                                          \
                        sqlite3_mprintf(                                                    \
                            "Expected a value of type %d but actual value is of type %d",   \
                            SQLITE_BLOB,                                                    \
                            type                                                            \
                        )                                                                   \
                    );                                                                      \
                    break;                                                                  \
            }                                                                               \
        }                                                                                   \
        __VA_ARGS__                                                                         \
    }

///////////////////////////////////////////////////////////////////////////
// Buffer helpers
///////////////////////////////////////////////////////////////////////////

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferRead(
    JNIEnv* env,
    jclass clazz,
    jlong buffer,
    jbyteArray destination,
    jint size,
    jlong sourceOffset,
    jint destinationOffset
) {
    const auto sourceAddress = LongToPtr(buffer);

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
    jlong buffer,
    jbyteArray source,
    jint size,
    jint sourceOffset,
    jlong destinationOffset
) {
    const auto destinationAddress = LongToPtr(buffer);

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

/**
 * Declares the function body for simple function call requiring jbyteArray conversion.
 */
#define ReturnWithByteArray(byteArray, length, function) \
    const auto UNDERSCORED(byteArray) = ByteArrayToBuffer(byteArray, length); \
    const auto result = function;          \
    sqlite3_free(UNDERSCORED(byteArray)); \
    return result

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
    int* outLength
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
    const void* pUtf8,
    size_t length
) {
    if (pUtf8 == nullptr) {
        return nullptr;
    }

    const auto utf8 = static_cast<const char*>(pUtf8);

    if (length == -1) {
        // This is what is used by sqlite internally
        length = 0x3fffffff & static_cast<int>(strlen(utf8));
    }

    const auto utf16Length = utf8_to_utf16_length(
        reinterpret_cast<const uint8_t*>(utf8),
        length
    );

    if (utf16Length <= 0) {
        const auto string = env->NewString(nullptr, 0);
        OutOfMemoryCheck(string != nullptr);
        return string;
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
    OutOfMemoryCheck(string != nullptr);
    sqlite3_free(utf16);

    return string;
}

// String must be freed
#define JstringToUtf8Out(string, outLength) jstringToUtf8(env, string, outLength)
// String must be freed
#define JstringToUtf8(string) JstringToUtf8Out(string, nullptr)
#define Utf8ToJstringLength(utf8, length) utf8ToJstring(env, utf8, length)
#define Utf8ToJstring(utf8) Utf8ToJstringLength(utf8, -1)
#define JstringCast(object) reinterpret_cast<jstring>(object)

/**
 * Declares the body for simple function call requiring jstring conversion.
 */
#define WithString(string, function) \
    int CONCAT(string, _size) = 0; \
    const auto UNDERSCORED(string) = JstringToUtf8Out(string, &CONCAT(string, _size)); \
    function;          \
    sqlite3_free(UNDERSCORED(string))

/**
 * Declares the body for simple function call requiring two jstring conversions.
 */
#define WithStrings(string1, string2, function) \
    WithString(string1, WithString(string2, function))

/**
 * Declares the function body for simple function call requiring jstring conversion and return the
 * result of the function.
 */
#define ReturnWithString(string, function) \
    WithString(string, const auto result = function); \
    return result

/**
 * Declares the body for simple function call requiring two jstring conversions and return the 
 * result of the function.
 */
#define ReturnWithStrings(string1, string2, function) \
    WithStrings(string1, string2, const auto result = function); \
    return result

///////////////////////////////////////////////////////////////////////////
// Primitives helpers
///////////////////////////////////////////////////////////////////////////

#define PrimitiveBoxInt(unboxedInt) env->NewObject(KJVI.klass, KJVI.constructor, unboxedInt)
#define PrimitiveBoxLong(unboxedLong) env->NewObject(KJVL.klass, KJVL.constructor, unboxedLong)

#define PrimitiveUnboxInt(boxedInt) env->CallIntMethod(boxedInt, KJVI.intValue)
#define PrimitiveUnboxLong(boxedLong) env->CallLongMethod(boxedLong, KJVL.longValue)

///////////////////////////////////////////////////////////////////////////
// Array helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns true if array length is equal to expected length, false otherwise.
 */
static inline bool checkArrayLength(
    JNIEnv* env,
    jobjectArray array,
    jsize expectedLength
) {
    RequireNonNullJobject(array);
    const auto length = env->GetArrayLength(array);
    return length == expectedLength;
}

/**
 * Throws an exception if array length differs from length.
 * This is intended to use if the array elements are all known.
 */
#define ArrayLengthEnsure(array, length) \
    if (!checkArrayLength(env, array, length)) \
        FatalError("Expected array to contains " #length " elements")

/**
 * Returns the object at the given index checking the instance type.
 */
static jobject getObjectFromArray(
    JNIEnv* const env,
    jobjectArray array,
    jint index,
    jclass klass
) {
    const auto object = env->GetObjectArrayElement(array, index);

    if (object == nullptr) {
        return nullptr;
    }

    if (!env->IsInstanceOf(object, klass)) {
        const auto className = MethodStringCall(klass, KJVC.getName);
        const auto buffer = JstringToUtf8(className);
        FatalError(sqlite3_mprintf("Object at index %d is not an instance of %s", index, buffer));
    }

    return object;
}

#define ArrayObjectGet(array, index, klass) getObjectFromArray(env, array, index, klass)

#define ArrayIntGet(array, index) \
    PrimitiveUnboxInt(RequireNonNullJobject(ArrayObjectGet(array, index, KJVI.klass)))

#define ArrayLongGet(array, index)  \
    PrimitiveUnboxLong(RequireNonNullJobject(ArrayObjectGet(array, index, KJVL.klass)))

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
static inline void outputPointerSetValue(
    JNIEnv* env,
    jobject pointer,
    jobject value
) {
    if (pointer != nullptr) {
        env->SetObjectField(pointer, KKOP.value, value);
        ExceptionClearAndAbort("Cannot set OutputPointer.value");
    }
}

#define OutputPointerGetValue(pointer) outputPointerGetValue(env, pointer)
#define OutputPointerSetValue(pointer, value) outputPointerSetValue(env, pointer, value)

#define OutputPointerGetStringValue(pointer) \
    JstringToUtf8(JstringCast(OutputPointerGetValue(pointer)))

#define OutputPointerSetStringValue(pointer, value, length) \
    OutputPointerSetValue(pointer, Utf8ToJstringLength(value, length))

/**
 * Gets the value of a 32bits integer OutputPointer.
 */
static jint outputPointerGetInt32Value(
    JNIEnv* env,
    jobject pointer
) {
    const auto boxedInt = RequireNonNullJobject(OutputPointerGetValue(pointer));
    const auto value = PrimitiveUnboxInt(boxedInt);
    ExceptionClearAndAbort("Failed to get the integer value from a boxed int");
    return value;
}

/**
 * Sets the value of a 32bits integer OutputPointer.
 */
static inline void outputPointerSetInt32Value(
    JNIEnv* env,
    jobject pointer,
    jint value
) {
    if (pointer != nullptr) {
        OutputPointerSetValue(pointer, PrimitiveBoxInt(value));
    }
}

#define OutputPointerGetInt32Value(pointer) outputPointerGetInt32Value(env, pointer)
#define OutputPointerSetInt32Value(pointer, value) outputPointerSetInt32Value(env, pointer, value)

/**
 * Gets the value of a 64bits integer OutputPointer.
 */
static jlong outputPointerGetInt64Value(
    JNIEnv* env,
    jobject pointer
) {
    const auto boxedLong = RequireNonNullJobject(OutputPointerGetValue(pointer));
    const auto value = PrimitiveUnboxLong(boxedLong);
    ExceptionClearAndAbort("Failed to get the long value from a boxed long");
    return value;
}

/**
 * Sets the value of a 64bits integer OutputPointer.
 */
static inline void outputPointerSetInt64Value(
    JNIEnv* env,
    jobject pointer,
    jlong value
) {
    if (pointer != nullptr) {
        OutputPointerSetValue(pointer, PrimitiveBoxLong(value));
    }
}

#define OutputPointerGetInt64Value(pointer) outputPointerGetInt64Value(env, pointer)
#define OutputPointerSetInt64Value(pointer, value) outputPointerSetInt64Value(env, pointer, value)

#define OutputPointerEnter(T, jPointer, getValue, transform) \
    const auto CONCAT(jPointer, _init) = transform(getValue(jPointer));                                                         \
    T* UNDERSCORED(jPointer) = nullptr; \
    if (jPointer != nullptr) *UNDERSCORED(jPointer) = CONCAT(jPointer, _init)

#define OutputPointerEnterInt32(jPointer) \
    OutputPointerEnter(jint, jPointer, OutputPointerGetInt32Value,)

#define OutputPointerEnterInt64(jPointer) \
    OutputPointerEnter(jlong, jPointer, OutputPointerGetInt64Value,)

#define OutputPointerEnterPointer(T, jPointer) \
    OutputPointerEnter(T, jPointer, OutputPointerGetInt64Value, reinterpret_cast<T>)

#define OutputPointerEnterString(jPointer, ...) \
    OutputPointerEnter(__VA_ARGS__ char*, jPointer, OutputPointerGetStringValue,)

#define OutputPointerEnterStringConst(jPointer) \
    OutputPointerEnterString(jPointer, const)

#define OutputPointerLeave(jPointer, setValue, transform, ...) \
    if (jPointer != nullptr && rc == SQLITE_OK) \
        setValue(jPointer, transform(*UNDERSCORED(jPointer)) __VA_OPT__(,) __VA_ARGS__)

#define OutputPointerLeaveInt32(jPointer) \
    OutputPointerLeave(jPointer, OutputPointerSetInt32Value,)

#define OutputPointerLeaveInt64(jPointer) \
    OutputPointerLeave(jPointer, OutputPointerSetInt64Value,)

#define OutputPointerLeavePointer(jPointer) \
    OutputPointerLeave(jPointer, OutputPointerSetInt64Value, PtrToLong,)

#define OutputPointerLeaveStringLength(jPointer, length) \
    OutputPointerLeave(jPointer, OutputPointerSetStringValue,,length); \
    sqlite3_free(CONCAT(jPointer, _init))

#define OutputPointerLeaveString(jPointer) \
    OutputPointerLeaveStringLength(jPointer, -1)

///////////////////////////////////////////////////////////////////////////
// Ksqlite 1 to 1 mapping
///////////////////////////////////////////////////////////////////////////

/**
 * Calls the `AutoExtensionCallback` hook.
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

        RequireObjectIsInstance(
            exception,
            KKJE.klass,
            "Unexpected exception type thrown in AutoExtensionCallback#call"
        );

        const auto message = MethodStringCall(exception, KKJE.message);

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
            HookConfigure(hook, callback, "(JJ)I", "AutoExtensionCallback");
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
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1prepare_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jbyteArray sql,
    jint maxBytes,
    jobject outStmt,
    jobject outOffset
) {
    const auto zSql = ByteArrayToBuffer(sql, maxBytes);
    OutputPointerEnterPointer(sqlite3_stmt*, outStmt);
    OutputPointerEnterInt32(outOffset);

    const auto rc = ksqlite_prepare_v2(
        LongTo_s3(db),
        reinterpret_cast<const char*>(zSql),
        maxBytes,
        outStmt_,
        outOffset_
    );

    OutputPointerLeaveInt32(outOffset);
    OutputPointerLeavePointer(outStmt);
    sqlite3_free(zSql);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1prepare_1v3(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jbyteArray sql,
    jint maxBytes,
    jint flags,
    jobject outStmt,
    jobject outOffset
) {
    const auto zSql = ByteArrayToBuffer(sql, maxBytes);
    OutputPointerEnterPointer(sqlite3_stmt*, outStmt);
    OutputPointerEnterInt32(outOffset);

    const auto rc = ksqlite_prepare_v3(
        LongTo_s3(db),
        reinterpret_cast<const char*>(zSql),
        maxBytes,
        flags,
        outStmt_,
        outOffset_
    );

    OutputPointerLeaveInt32(outOffset);
    OutputPointerLeavePointer(outStmt);
    sqlite3_free(zSql);

    return rc;
}

///////////////////////////////////////////////////////////////////////////
// SQLite 1 to 1 mapping
///////////////////////////////////////////////////////////////////////////

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1aggregate_1context(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jboolean create
) {
    const auto s3Context = LongTo_s3_context(context);
    void* pointer;

    if (create) {
        pointer = sqlite3_aggregate_context(s3Context, sizeof(void*));
    } else {
        pointer = sqlite3_aggregate_context(s3Context, 0);
    }

    return PtrToLong(pointer);
}

/**
 * Calls the `AutoVacuumPagesCallback` hook.
 */
static unsigned int autoVacuumPagesCaller(
    void* pHook,
    const char* zSchema,
    unsigned int nDbPage,
    unsigned int nFreePage,
    unsigned int nBytePerPage
) {
    JniEnvDeclare();
    DbStateDeclareHook(pHook);
    const auto schema = Utf8ToJstring(zSchema);

    HookEnterDbState(autoVacuumPages);
    uint result = env->CallIntMethod(instance, call, schema, nDbPage, nFreePage, nBytePerPage);
    HookLeave();

    LocalRefDestroy(schema);
    return result;
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
    DbHookDestructorReplace(
        autoVacuumPages,
        "(Ljava/lang/String;III)I",
        "AutoVacuumPagesCallback",
        sqlite3_autovacuum_pages(pDb, autoVacuumPagesCaller, pHook, hookDestroyer),
        sqlite3_autovacuum_pages(pDb, nullptr, nullptr, nullptr)
    );
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
    ReturnWithStrings(
        destDbName,
        srcDbName,
        PtrToLong(sqlite3_backup_init(LongTo_s3(destDb), destDbName_, LongTo_s3(srcDb), srcDbName_))
    );
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
    jbyteArray bytes,
    jint size,
    jobject destructor
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto pBuffer = ByteArrayToBuffer(bytes, size);
    const auto freeable = AllocateFreeablePointer(pBuffer, destructor);
    const auto destroyer = FreeableDestroyerPush(pBuffer, freeable);

    return sqlite3_bind_blob(pStmt, index, pBuffer, size, destroyer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1blob64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong buffer,
    jlong size,
    jobject destructor
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto pBuffer = LongToPtr(buffer);
    const auto freeable = AllocateFreeablePointer(nullptr, destructor);
    const auto destroyer = FreeableDestroyerPush(pBuffer, freeable);

    return sqlite3_bind_blob64(pStmt, index, pBuffer, size, destroyer);
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
    ReturnWithString(name, sqlite3_bind_parameter_index(LongTo_s3_stmt(stmt), name_));
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
    jstring value
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    int bufferSize = 0;
    const auto buffer = JstringToUtf8Out(value, &bufferSize);
    const auto freeable = AllocateFreeablePointer(buffer, nullptr);
    const auto pDestructor = FreeableDestroyerPush(buffer, freeable);

    return sqlite3_bind_text(pStmt, index, buffer, bufferSize, pDestructor);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1text64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong buffer,
    jlong size,
    jobject destructor,
    jint encoding
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto pBuffer = reinterpret_cast<char*>(LongToPtr(buffer));
    const auto freeable = AllocateFreeablePointer(nullptr, destructor);
    const auto destroyer = FreeableDestroyerPush(pBuffer, freeable);

    return sqlite3_bind_text64(pStmt, index, pBuffer, size, destroyer, encoding);
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

    OutputPointerEnterPointer(sqlite3_blob*, outBlob);
    const auto rc = sqlite3_blob_open(pDb, zDb, zTable, zColumn, rowIndex, flags, outBlob_);
    OutputPointerLeavePointer(outBlob);

    sqlite3_free(zDb);
    sqlite3_free(zTable);
    sqlite3_free(zColumn);

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
    ReturnWithByteArray(
        buffer,
        size,
        sqlite3_blob_write(LongTo_s3_blob(blob), buffer_, size, offset)
    );
}

/**
 * Calls the `BusyHandlerCallback` hook.
 */
static int busyHandlerCaller(
    void* pDbStateHook,
    int n
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    HookEnterDbState(busyHandler);
    jint result = env->CallIntMethod(instance, call, n);
    HookLeave();

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1busy_1handler(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceResultCode(
        busyHandler,
        "(I)I",
        "BusyHandlerCallback",
        sqlite3_busy_handler(pDb, busyHandlerCaller, pDbState),
        sqlite3_busy_handler(pDb, nullptr, nullptr)
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
    const auto pDb = LongTo_s3(db);
    const auto rc = sqlite3_close(pDb);

    if (rc == SQLITE_OK && pDb != nullptr) {
        popDbState(env, pDb);
    }

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1close_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    const auto pDb = LongTo_s3(db);
    const auto rc = sqlite3_close_v2(pDb);

    if (rc == SQLITE_OK && pDb != nullptr) {
        popDbState(env, pDb);
    }

    return rc;
}

/**
 * Calls the `CollationNeededCallback` hook.
 */
static void collationNeededCaller(
    void* pDbStateHook,
    sqlite3* pDb,
    int eTextRep,
    const char* zName
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    const auto db = PtrToLong(pDb);
    const auto name = Utf8ToJstring(zName);

    HookEnterDbState(collationNeeded);
    env->CallVoidMethod(instance, call, db, eTextRep, zName);
    HookLeave();

    LocalRefDestroy(name);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1collation_1needed(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceResultCode(
        busyHandler,
        "(JILjava/lang/String;)V",
        "CollationNeededCallback",
        sqlite3_collation_needed(pDb, pDbState, collationNeededCaller),
        sqlite3_collation_needed(pDb, nullptr, nullptr)
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

    BufferBlobDeclare(
        jbyteArray,
        KJV.emptyByteArray,
        BufferToByteArray(pointer, length),
        sqlite3_column_blob(pStmt, index),
        sqlite3_column_bytes(pStmt, index),
        sqlite3_column_type(pStmt, index)
    )

    return result;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1column_1buffer(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject outSize
) {
    const auto pStmt = LongTo_s3_stmt(stmt);

    BufferBlobDeclare(
        void*,
        KK.emptyBufferPointer,
        BufferToByteArray(pointer, length),
        sqlite3_column_blob(pStmt, index),
        sqlite3_column_bytes(pStmt, index),
        sqlite3_column_type(pStmt, index),
        OutputPointerSetInt64Value(outSize, length);
    )

    return PtrToLong(result);
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

/**
 * Calls the `CommitHookCallback` hook.
 */
static int commitHookCaller(void* pDbStateHook) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    HookEnterDbState(commitHook);
    jint result = env->CallIntMethod(instance, call);
    HookLeave();

    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1commit_1hook(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceInstance(
        commitHook,
        "()I",
        "CommitHookCallback",
        sqlite3_commit_hook(pDb, commitHookCaller, pDbState),
        sqlite3_commit_hook(pDb, nullptr, nullptr)
    );
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1compileoption_1get(
    JNIEnv* env,
    jclass clazz,
    jint index
) {
    return Utf8ToJstring(sqlite3_compileoption_get(index));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1compileoption_1used(
    JNIEnv* env,
    jclass clazz,
    jstring name
) {
    ReturnWithString(name, sqlite3_compileoption_used(name_));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1complete(
    JNIEnv* env,
    jclass clazz,
    jstring sql
) {
    ReturnWithString(sql, sqlite3_complete(sql_));
}

/**
 * Calls the `ConfigLogCallback` hook.
 */
static void configLogCaller(
    void*,
    int errCode,
    const char* z
) {
    JniEnvDeclare();
    const auto message = Utf8ToJstring(z);

    HookEnter(K.hooks, K.hooks.log);
    env->CallVoidMethod(instance, call, errCode, message);
    HookLeave();

    LocalRefDestroy(message);
}

/**
 * Calls the `ConfigSqlLogCallback` hook.
 */
static void configSqlLogCaller(
    void*,
    sqlite3* pDb,
    const char* z,
    int op
) {
    JniEnvDeclare();
    const auto db = PtrToLong(pDb);
    const auto message = Utf8ToJstring(z);

    HookEnter(K.hooks, K.hooks.sqlLog);
    env->CallVoidMethod(instance, call, db, message, op);
    HookLeave();

    LocalRefDestroy(message);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1config(
    JNIEnv* env,
    jclass clazz,
    jint id,
    jobjectArray args
) {
    switch (id) {
        // []
        case SQLITE_CONFIG_SINGLETHREAD:
        case SQLITE_CONFIG_MULTITHREAD:
        case SQLITE_CONFIG_SERIALIZED: {
            ArrayLengthEnsure(args, 0);
            return sqlite3_config(id);
        }

            // [Long, Int, Int]
        case SQLITE_CONFIG_PAGECACHE:
        case SQLITE_CONFIG_HEAP: {
            ArrayLengthEnsure(args, 3);
            return sqlite3_config(
                id,
                LongToPtr(ArrayLongGet(args, 0)),
                ArrayIntGet(args, 1),
                ArrayIntGet(args, 2)
            );
        }

            // [Int]
        case SQLITE_CONFIG_MEMSTATUS:
        case SQLITE_CONFIG_URI:
        case SQLITE_CONFIG_COVERING_INDEX_SCAN:
        case SQLITE_CONFIG_STMTJRNL_SPILL:
        case SQLITE_CONFIG_SMALL_MALLOC:
        case SQLITE_CONFIG_SORTERREF_SIZE: {
            ArrayLengthEnsure(args, 1);
            return sqlite3_config(id, ArrayIntGet(args, 0));
        }

            // [Long]
        case SQLITE_CONFIG_MEMDB_MAXSIZE: {
            ArrayLengthEnsure(args, 1);
            return sqlite3_config(id, ArrayLongGet(args, 0));
        }

            // [OutputPointer.OfInt32]
        case SQLITE_CONFIG_ROWID_IN_VIEW: {
            ArrayLengthEnsure(args, 1);
            const auto jPointer = ArrayObjectGet(args, 0, KKOP.klass);
            OutputPointerEnterInt32(jPointer);
            const auto rc = sqlite3_config(id, jPointer_);
            OutputPointerLeaveInt32(jPointer);
            return rc;
        }

            // [UInt]
        case SQLITE_CONFIG_WIN32_HEAPSIZE:
        case SQLITE_CONFIG_PMASZ: {
            ArrayLengthEnsure(args, 1);
            return sqlite3_config(id, static_cast<uint>(ArrayIntGet(args, 0)));
        }

            // [Long, Long]
        case SQLITE_CONFIG_MMAP_SIZE: {
            ArrayLengthEnsure(args, 2);
            return sqlite3_config(id, ArrayLongGet(args, 0), ArrayLongGet(args, 1));
        }

            // [Int, Int]
        case SQLITE_CONFIG_LOOKASIDE: {
            ArrayLengthEnsure(args, 2);
            return sqlite3_config(id, ArrayIntGet(args, 0), ArrayIntGet(args, 1));
        }

            // [ConfigLogCallback]
        case SQLITE_CONFIG_LOG: {
            ArrayLengthEnsure(args, 1);
            const auto callback = ArrayObjectGet(args, 0, KK.configLogCallback);
            GlobalHookReplaceRC(
                log,
                "(ILjava/lang/String;)V",
                "ConfigLogCallback",
                sqlite3_config(id, configLogCaller, nullptr);
            );
        }

            // [ConfigSqlLogCallback]
        case SQLITE_CONFIG_SQLLOG: {
            ArrayLengthEnsure(args, 1);
            const auto callback = ArrayObjectGet(args, 0, KK.configSqlLogCallback);
            GlobalHookReplaceRC(
                sqlLog,
                "(JLjava/lang/String;I)V",
                "ConfigSqlLogCallback",
                sqlite3_config(id, configSqlLogCaller, nullptr);
            );
        }

        default:
            return SQLITE_MISUSE;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1context_1db_1handle(
    JNIEnv* env,
    jclass clazz,
    jlong context
) {
    return PtrToLong(sqlite3_context_db_handle(LongTo_s3_context(context)));
}

/**
 * Calls the `CollationCompareCallback` hook.
 */
static int collationCompareCaller(
    void* pHook,
    int nLhs,
    const void* lhs,
    int nRhs,
    const void* rhs
) {
    JniEnvDeclare();
    DbStateDeclareHook(pHook);

    const auto lhsByteArray = BufferToByteArray(lhs, nLhs);
    const auto rhsByteArray = BufferToByteArray(rhs, nRhs);

    HookEnterDbState(collationCompare);
    jint result = env->CallIntMethod(instance, call, lhsByteArray, rhsByteArray);
    HookLeave();

    env->DeleteLocalRef(lhsByteArray);
    env->DeleteLocalRef(rhsByteArray);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1create_1collation_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jint eTextRep,
    jobject destructor,
    jobject callback
) {
    const auto zName = JstringToUtf8(name);

    DbHookDestructorReplace(
        autoVacuumPages,
        "([B[B)I",
        "CollationCompareCallback",
        sqlite3_create_collation_v2(
            pDb,
            zName,
            eTextRep,
            pHook,
            collationCompareCaller,
            hookDestroyer
        ),
        sqlite3_create_collation_v2(pDb, zName, eTextRep, nullptr, nullptr, nullptr),
        sqlite3_free(zName);
    );
}

/**
 * Calls the `FunctionCallback.Func` hook.
 */
static void functionFuncCaller(
    sqlite3_context* pContext,
    int argc,
    sqlite3_value** argv
) {
    FunctionDeclareHook(Function, func);
    callFunctionFunc2(hook, pContext, argc, argv);
}

/**
 * Calls the `FunctionCallback.Step` hook.
 */
static void functionStepCaller(
    sqlite3_context* pContext,
    int argc,
    sqlite3_value** argv
) {
    FunctionDeclareHook(Function, step);
    callFunctionFunc2(hook, pContext, argc, argv);
}

/**
 * Calls the `FunctionCallback.Final` hook.
 */
static void functionFinalCaller(sqlite3_context* pContext) {
    FunctionDeclareHook(Function, final);
    callFunctionFunc1(hook, pContext);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1create_1function_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jint nArg,
    jint eTextRep,
    jobject appData,
    jobject func,
    jobject step,
    jobject final,
    jobject destroy
) {
    // True for aggregate, false for scalar.
    bool isAggregate = false;

    if (func != nullptr) { // Scalar function
        if (step != nullptr || final != nullptr) {
            return SQLITE_MISUSE; // Invalid scalar function
        }
    } else {
        if (step == nullptr && final == nullptr) { // Function deletion
            if (destroy != nullptr) {
                return SQLITE_MISUSE; // No destructor is allowed here
            }

            ReturnWithString(name, sqlite3_create_function_v2(
                LongTo_s3(db),
                name_,
                nArg,
                eTextRep,
                nullptr,
                nullptr,
                nullptr,
                nullptr,
                nullptr
            ));
        } else if (step == nullptr || final == nullptr) {
            return SQLITE_MISUSE; // Invalid aggregate function
        } else {
            isAggregate = true;
        }
    }

    const auto pDb = LongTo_s3(db);
    const auto zName = JstringToUtf8(name);
    const auto pFunction = allocateFunction(env, appData, destroy, KKDC.destroy);
    auto& function = *pFunction;
    auto rc = SQLITE_OK;

    if (isAggregate) {
        FunctionStepHookConfigure(step)
        FunctionFinalHookConfigure(final)

        rc = sqlite3_create_function_v2(
            pDb,
            zName,
            nArg,
            eTextRep,
            pFunction,
            nullptr,
            functionStepCaller,
            functionFinalCaller,
            functionDestroyer
        );
    } else {
        FunctionFuncHookConfigure(func)

        rc = sqlite3_create_function_v2(
            pDb,
            zName,
            nArg,
            eTextRep,
            pFunction,
            functionFuncCaller,
            nullptr,
            nullptr,
            functionDestroyer
        );
    }

    if (rc == SQLITE_MISUSE) {
        // The destructor is not called by sqlite after a misuse
        // Cleanup the function but do not call application destructor
        DestroyableClear(function);
        destroyFunction(env, pFunction);
    }

    sqlite3_free(zName);
    return rc;
}

/*
extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1create_1module_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jlong module,
    jobject appData,
    jobject destroy
) {
    // TODO: implement sqlite3_create_module_v2()
}*/

/**
 * Calls the `FunctionCallback.Inverse` hook.
 */
static void functionInverseCaller(
    sqlite3_context* pContext,
    int argc,
    sqlite3_value** argv
) {
    FunctionDeclareHook(FunctionWindow, inverse);
    callFunctionFunc2(hook, pContext, argc, argv);
}

/**
 * Calls the `FunctionCallback.Value` hook.
 */
static void functionValueCaller(sqlite3_context* pContext) {
    FunctionDeclareHook(FunctionWindow, value);
    callFunctionFunc1(hook, pContext);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1create_1window_1function(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jint nArg,
    jint eTextRep,
    jobject appData,
    jobject step,
    jobject final,
    jobject value,
    jobject inverse,
    jobject destroy
) {
    if (step == nullptr && final == nullptr && value == nullptr && inverse == nullptr) {
        // Function deletion
        if (destroy != nullptr) {
            return SQLITE_MISUSE; // No destructor is allowed here
        }

        ReturnWithString(name, sqlite3_create_window_function(
            LongTo_s3(db),
            name_,
            nArg,
            eTextRep,
            nullptr,
            nullptr,
            nullptr,
            nullptr,
            nullptr,
            nullptr
        ));
    } else if (step == nullptr || final == nullptr || value == nullptr || inverse == nullptr) {
        return SQLITE_MISUSE; // All parameters are required
    }

    const auto pFunction = allocateFunctionWindow(env, appData, destroy, KKDC.destroy);
    auto& function = *pFunction;
    const auto zName = JstringToUtf8(name);

    FunctionStepHookConfigure(step)
    FunctionFinalHookConfigure(final)
    FunctionValueHookConfigure(value)
    FunctionInverseHookConfigure(inverse)

    auto rc = sqlite3_create_window_function(
        LongTo_s3(db),
        zName,
        nArg,
        eTextRep,
        pFunction,
        functionStepCaller,
        functionFinalCaller,
        functionValueCaller,
        functionInverseCaller,
        functionWindowDestroyer
    );

    if (rc == SQLITE_MISUSE) {
        // The destructor is not called by sqlite after a misuse
        // Cleanup the function but do not call application destructor
        DestroyableClear(function);
        destroyFunctionWindow(env, pFunction);
    }

    sqlite3_free(zName);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1data_1count(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_data_count(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1cacheflush(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_db_cacheflush(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1config(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint option,
    jobjectArray args
) {
    const auto pDb = LongTo_s3(db);

    switch (option) {
        // [String]
        case SQLITE_DBCONFIG_MAINDBNAME: {
            ArrayLengthEnsure(args, 1);
            const auto name = JstringCast(ArrayObjectGet(args, 0, KJV.string));

            DbStateMutexEnter(pDb);
            sqlite3_free(dbState.configMainDbName);

            dbState.configMainDbName = JstringToUtf8(name);
            const auto rc = sqlite3_db_config(pDb, option, dbState.configMainDbName);

            if (rc != SQLITE_OK) {
                sqlite3_free(dbState.configMainDbName);
            }

            DbStateMutexLeave();
            return rc;
        }

            // [Long, Int, Int]
        case SQLITE_CONFIG_LOOKASIDE: {
            ArrayLengthEnsure(args, 3);
            return sqlite3_db_config(
                pDb,
                option,
                LongToPtr(ArrayLongGet(args, 0)),
                ArrayIntGet(args, 1),
                ArrayIntGet(args, 2)
            );
        }

            // [Int, OutputPointer.OfInt32]
        case SQLITE_DBCONFIG_ENABLE_FKEY:
        case SQLITE_DBCONFIG_ENABLE_TRIGGER:
        case SQLITE_DBCONFIG_ENABLE_FTS3_TOKENIZER:
        case SQLITE_DBCONFIG_ENABLE_LOAD_EXTENSION:
        case SQLITE_DBCONFIG_NO_CKPT_ON_CLOSE:
        case SQLITE_DBCONFIG_ENABLE_QPSG:
        case SQLITE_DBCONFIG_TRIGGER_EQP:
        case SQLITE_DBCONFIG_RESET_DATABASE:
        case SQLITE_DBCONFIG_DEFENSIVE:
        case SQLITE_DBCONFIG_WRITABLE_SCHEMA:
        case SQLITE_DBCONFIG_LEGACY_ALTER_TABLE:
        case SQLITE_DBCONFIG_DQS_DML:
        case SQLITE_DBCONFIG_DQS_DDL:
        case SQLITE_DBCONFIG_ENABLE_VIEW:
        case SQLITE_DBCONFIG_LEGACY_FILE_FORMAT:
        case SQLITE_DBCONFIG_TRUSTED_SCHEMA:
        case SQLITE_DBCONFIG_STMT_SCANSTATUS:
        case SQLITE_DBCONFIG_REVERSE_SCANORDER:
        case SQLITE_DBCONFIG_ENABLE_ATTACH_CREATE:
        case SQLITE_DBCONFIG_ENABLE_ATTACH_WRITE:
        case SQLITE_DBCONFIG_ENABLE_COMMENTS: {
            ArrayLengthEnsure(args, 2);
            const auto value = ArrayIntGet(args, 0);
            const auto jPointer = ArrayObjectGet(args, 1, KKOP.klass);
            OutputPointerEnterInt32(jPointer);
            const auto rc = sqlite3_db_config(pDb, option, value, jPointer_);
            OutputPointerLeaveInt32(jPointer);
            return rc;
        }
        default:
            return SQLITE_MISUSE;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1filename(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name
) {
    ReturnWithString(name, Utf8ToJstring(sqlite3_db_filename(LongTo_s3(db), name_)));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1handle(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return PtrToLong(sqlite3_db_handle(LongTo_s3_stmt(stmt)));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1name(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint index
) {
    return Utf8ToJstring(sqlite3_db_name(LongTo_s3(db), index));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1readonly(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name
) {
    ReturnWithString(name, sqlite3_db_readonly(LongTo_s3(db), name_));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1release_1memory(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_db_release_memory(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1status(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint option,
    jobject outCurrent,
    jobject outHighwater,
    jint resetFlag
) {
    const auto pDb = LongTo_s3(db);
    OutputPointerEnterInt32(outCurrent);
    OutputPointerEnterInt32(outHighwater);
    const auto rc = sqlite3_db_status(pDb, option, outCurrent_, outHighwater_, resetFlag);
    OutputPointerLeaveInt32(outCurrent);
    OutputPointerLeaveInt32(outHighwater);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1db_1status64(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint option,
    jobject outCurrent,
    jobject outHighwater,
    jint resetFlag
) {
    const auto pDb = LongTo_s3(db);
    OutputPointerEnterInt64(outCurrent);
    OutputPointerEnterInt64(outHighwater);
    const auto rc = sqlite3_db_status64(pDb, option, outCurrent_, outHighwater_, resetFlag);
    OutputPointerLeaveInt64(outCurrent);
    OutputPointerLeaveInt64(outHighwater);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1declare_1vtab(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring sql
) {
    ReturnWithString(sql, sqlite3_declare_vtab(LongTo_s3(db), sql_));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1deserialize(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring schema,
    jlong buffer,
    jlong dbSize,
    jlong bufferSize,
    jint flags
) {
    ReturnWithString(schema, sqlite3_deserialize(
        LongTo_s3(db),
        schema_,
        static_cast<unsigned char*>(LongToPtr(buffer)),
        dbSize,
        bufferSize,
        flags
    ));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1errcode(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_errcode(LongTo_s3(db));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1errmsg(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return Utf8ToJstring(sqlite3_errmsg(LongTo_s3(db)));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1error_1offset(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_error_offset(LongTo_s3(db));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1errstr(
    JNIEnv* env,
    jclass clazz,
    jint resultCode
) {
    return Utf8ToJstring(sqlite3_errstr(resultCode));
}

/**
 * Calls the `ExecCallback` hook.
 */
static int execCaller(
    void* pCallback,
    int argc,
    char** argv,
    char** argn
) {
    JniEnvDeclare();

    const auto callback = reinterpret_cast<jobject>(pCallback);
    const auto values = env->NewObjectArray(argc, KJV.string, nullptr);
    const auto names = env->NewObjectArray(argc, KJV.string, nullptr);

    OutOfMemoryCheck(values != nullptr);
    OutOfMemoryCheck(names != nullptr);

    for (int i = 0; i < argc; ++i) {
        jstring string = nullptr;

        if (argv[i] != nullptr) {
            string = Utf8ToJstring(argv[i]);
            env->SetObjectArrayElement(values, i, string);
            env->DeleteLocalRef(string);
        }

        string = Utf8ToJstring(argn[i]);
        env->SetObjectArrayElement(names, i, string);
        env->DeleteLocalRef(string);
    }

    auto result = env->CallIntMethod(callback, KKEC.call, argc, values, names);

    env->DeleteLocalRef(values);
    env->DeleteLocalRef(names);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1exec(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring sql,
    jobject callback,
    jobject errorMessage
) {
    const auto pDb = LongTo_s3(db);
    const auto zSql = JstringToUtf8(sql);

    OutputPointerEnterString(errorMessage);
    const auto rc = sqlite3_exec(pDb, zSql, execCaller, callback, errorMessage_);
    OutputPointerLeaveString(errorMessage);

    sqlite3_free(*errorMessage_);
    sqlite3_free(zSql);

    return rc;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1expanded_1sql(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    const auto pSql = sqlite3_expanded_sql(LongTo_s3_stmt(stmt));
    const auto sql = Utf8ToJstring(pSql);
    sqlite3_free(pSql);
    return sql;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1extended_1errcode(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_extended_errcode(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1extended_1result_1codes(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint enabled
) {
    return sqlite3_extended_result_codes(LongTo_s3(db), enabled);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1file_1control(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jint opcode
) {
    ReturnWithString(name, sqlite3_file_control(LongTo_s3(db), name_, opcode, nullptr));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1finalize(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_finalize(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1free(
    JNIEnv* env,
    jclass clazz,
    jlong buffer
) {
    sqlite3_free(LongToPtr(buffer));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1get_1autocommit(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_get_autocommit(LongTo_s3(db));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1hard_1heap_1limit64(
    JNIEnv* env,
    jclass clazz,
    jlong limit
) {
    return sqlite3_hard_heap_limit64(limit);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1get_1auxdata(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint index
) {
    return PtrToLong(sqlite3_get_auxdata(LongTo_s3_context(context), index));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1initialize(
    JNIEnv* env,
    jclass clazz
) {
    return sqlite3_initialize();
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1interrupt(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    sqlite3_interrupt(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1is_1interrupted(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_is_interrupted(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1key(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jbyteArray key,
    jint nKey
) {
    ReturnWithByteArray(key, nKey, sqlite3_key(LongTo_s3(db), key_, nKey));
}

/**
 * Common handling for `sqlite3_key_v2()` and `sqlite3_rekey_v2()`.
 */
static jint keyOrRekeyV2(
    JNIEnv* env,
    jlong db,
    jstring dbName,
    jbyteArray key,
    jint nKey,
    int (* callback)(sqlite3* db, const char* zDbName, const void* pKey, int nKey)
) {
    const auto pDb = LongTo_s3(db);
    const auto zName = JstringToUtf8(dbName);
    const auto pKey = ByteArrayToBuffer(key, nKey);
    const auto rc = callback(pDb, zName, pKey, nKey);

    sqlite3_free(zName);
    sqlite3_free(pKey);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1key_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring dbName,
    jbyteArray key,
    jint nKey
) {
    return keyOrRekeyV2(env, db, dbName, key, nKey, sqlite3_key_v2);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1keyword_1check(
    JNIEnv* env,
    jclass clazz,
    jstring word
) {
    ReturnWithString(word, sqlite3_keyword_check(word_, word_size));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1keyword_1count(
    JNIEnv* env,
    jclass clazz
) {
    return sqlite3_keyword_count();
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1keyword_1name(
    JNIEnv* env,
    jclass clazz,
    jint index,
    jobject name
) {
    OutputPointerEnterStringConst(name);
    int length = 0;
    const auto rc = sqlite3_keyword_name(index, name_, &length);
    OutputPointerLeaveStringLength(name, length);

    return rc;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1last_1insert_1rowid(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_last_insert_rowid(LongTo_s3(db));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1libversion(
    JNIEnv* env,
    jclass clazz
) {
    return Utf8ToJstring(sqlite3_libversion());
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1libversion_1number(
    JNIEnv* env,
    jclass clazz
) {
    return sqlite3_libversion_number();
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1limit(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint id,
    jint newVal
) {
    return sqlite3_limit(LongTo_s3(db), id, newVal);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1log(
    JNIEnv* env,
    jclass clazz,
    jint errorCode,
    jstring message
) {
    WithString(message, sqlite3_log(errorCode, message_));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1malloc(
    JNIEnv* env,
    jclass clazz,
    jint size
) {
    return PtrToLong(sqlite3_malloc(size));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1malloc64(
    JNIEnv* env,
    jclass clazz,
    jlong size
) {
    return PtrToLong(sqlite3_malloc64(size));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1memory_1used(
    JNIEnv* env,
    jclass clazz
) {
    return sqlite3_memory_used();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1memory_1highwater(
    JNIEnv* env,
    jclass clazz,
    jint resetFlag
) {
    return sqlite3_memory_highwater(resetFlag);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1msize(
    JNIEnv* env,
    jclass clazz,
    jlong buffer
) {
    return static_cast<jlong>(sqlite3_msize(LongToPtr(buffer)));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1next_1stmt(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jlong stmt
) {
    return PtrToLong(sqlite3_next_stmt(LongTo_s3(db), LongTo_s3_stmt(stmt)));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1open(
    JNIEnv* env,
    jclass clazz,
    jstring fileName,
    jobject outDb
) {
    const auto zFileName = JstringToUtf8(fileName);

    OutputPointerEnterPointer(sqlite3*, outDb);
    const auto rc = sqlite3_open(zFileName, outDb_);
    OutputPointerLeavePointer(outDb);

    sqlite3_free(zFileName);

    if (rc == SQLITE_OK) {
        pushDbState(env, *outDb_);
    }

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1open_1v2(
    JNIEnv* env,
    jclass clazz,
    jstring fileName,
    jobject outDb,
    jint flags,
    jstring vfs
) {
    const auto zFileName = JstringToUtf8(fileName);
    const auto zVfs = JstringToUtf8(vfs);

    OutputPointerEnterPointer(sqlite3*, outDb);
    const auto rc = sqlite3_open_v2(zFileName, outDb_, flags, zVfs);
    OutputPointerLeavePointer(outDb);

    sqlite3_free(zVfs);
    sqlite3_free(zFileName);

    if (rc == SQLITE_OK) {
        pushDbState(env, *outDb_);
    }

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1overload_1function(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jint nArg
) {
    ReturnWithString(name, sqlite3_overload_function(LongTo_s3(db), name_, nArg));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1prepare_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring sql,
    jobject outStmt
) {
    const auto pDb = LongTo_s3(db);
    int nByte = 0;
    const auto zSql = JstringToUtf8Out(sql, &nByte);

    OutputPointerEnterPointer(sqlite3_stmt*, outStmt);
    const auto rc = sqlite3_prepare_v2(pDb, zSql, nByte, outStmt_, nullptr);
    OutputPointerLeavePointer(outStmt);
    sqlite3_free(zSql);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1prepare_1v3(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring sql,
    jint flags,
    jobject outStmt
) {
    const auto pDb = LongTo_s3(db);
    int nByte = 0;
    const auto zSql = JstringToUtf8Out(sql, &nByte);

    OutputPointerEnterPointer(sqlite3_stmt*, outStmt);
    const auto rc = sqlite3_prepare_v3(pDb, zSql, nByte, flags, outStmt_, nullptr);
    OutputPointerLeavePointer(outStmt);
    sqlite3_free(zSql);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1preupdate_1blobwrite(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_preupdate_blobwrite(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1preupdate_1count(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_preupdate_count(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1preupdate_1depth(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_preupdate_depth(LongTo_s3(db));
}

/**
 * Calls the `PreupdateHookCallback` hook.
 */
static void preupdateHookCaller(
    void* pDbStateHook,
    sqlite3* db,
    int op,
    char const* zDb,
    char const* zName,
    sqlite3_int64 iKey1,
    sqlite3_int64 iKey2
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    const auto pDb = PtrToLong(db);
    const auto dbName = Utf8ToJstring(zDb);
    const auto dbTable = Utf8ToJstring(zName);

    HookEnterDbState(preupdateHook);
    env->CallVoidMethod(instance, call, pDb, op, dbName, dbTable, iKey1, iKey2);
    HookLeave();

    LocalRefDestroy(dbName);
    LocalRefDestroy(dbTable);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1preupdate_1hook(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceInstance(
        preupdateHook,
        "(JILjava/lang/String;Ljava/lang/String;JJ)V",
        "PreupdateHookCallback",
        sqlite3_preupdate_hook(pDb, preupdateHookCaller, pDbState),
        sqlite3_preupdate_hook(pDb, nullptr, nullptr)
    );
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1preupdate_1new(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint index,
    jobject outValue
) {
    OutputPointerEnterPointer(sqlite3_value*, outValue);
    const auto rc = sqlite3_preupdate_new(LongTo_s3(db), index, outValue_);
    OutputPointerLeavePointer(outValue);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1preupdate_1old(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint index,
    jobject outValue
) {
    OutputPointerEnterPointer(sqlite3_value*, outValue);
    const auto rc = sqlite3_preupdate_old(LongTo_s3(db), index, outValue_);
    OutputPointerLeavePointer(outValue);

    return rc;
}

/**
 * Calls the `ProgressHandlerCallback` hook.
 */
static int progressHandlerCaller(void* pDbStateHook) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    HookEnterDbState(progressHandler);
    const auto result = env->CallIntMethod(instance, call);
    HookLeave();

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1progress_1handler(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint nOps,
    jobject callback
) {
    DbHookReplace(
        preupdateHook, ,
        HookConfigure(hook, callback, "()I", "ProgressHandlerCallback");,
        sqlite3_progress_handler(pDb, nOps, progressHandlerCaller, nullptr);,
        sqlite3_progress_handler(pDb, 0, nullptr, nullptr);,
    )
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1randomness(
    JNIEnv* env,
    jclass clazz,
    jint size,
    jlong buffer
) {
    sqlite3_randomness(size, LongToPtr(buffer));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1realloc(
    JNIEnv* env,
    jclass clazz,
    jlong buffer,
    jint size
) {
    return PtrToLong(sqlite3_realloc(LongToPtr(buffer), size));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1realloc64(
    JNIEnv* env,
    jclass clazz,
    jlong buffer,
    jlong size
) {
    return PtrToLong(sqlite3_realloc64(LongToPtr(buffer), size));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1rekey(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jbyteArray key,
    jint nKey
) {
    ReturnWithByteArray(key, nKey, sqlite3_rekey(LongTo_s3(db), key_, nKey));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1rekey_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring dbName,
    jbyteArray key,
    jint nKey
) {
    return keyOrRekeyV2(env, db, dbName, key, nKey, sqlite3_rekey_v2);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1release_1memory(
    JNIEnv* env,
    jclass clazz,
    jint size
) {
    return sqlite3_release_memory(size);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1reset(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_reset(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1reset_1auto_1extension(
    JNIEnv* env,
    jclass clazz
) {
    MutexEnter(KHS);
    auto& hook = KHS.autoExtension;

    if (hook.instance != nullptr) {
        sqlite3_reset_auto_extension();
        HookClear(hook);
    }

    MutexLeave(KHS);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1blob(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jbyteArray bytes,
    jint size,
    jobject destructor
) {
    const auto pContext = LongTo_s3_context(context);
    const auto pBuffer = ByteArrayToBuffer(bytes, size);
    const auto freeable = AllocateFreeablePointer(pBuffer, destructor);
    const auto destroyer = FreeableDestroyerPush(pBuffer, freeable);

    sqlite3_result_blob(pContext, pBuffer, size, destroyer);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1blob64(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jlong buffer,
    jlong size,
    jobject destructor
) {
    const auto pContext = LongTo_s3_context(context);
    const auto pBuffer = LongToPtr(buffer);
    const auto freeable = AllocateFreeablePointer(nullptr, destructor);
    const auto destroyer = FreeableDestroyerPush(pBuffer, freeable);

    sqlite3_result_blob64(pContext, pBuffer, size, destroyer);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1double(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jdouble value
) {
    sqlite3_result_double(LongTo_s3_context(context), value);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1error(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jstring message
) {
    WithString(message, sqlite3_result_error(LongTo_s3_context(context), message_, message_size));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1error_1code(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint errorCode
) {
    sqlite3_result_error_code(LongTo_s3_context(context), errorCode);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1error_1nomem(
    JNIEnv* env,
    jclass clazz,
    jlong context
) {
    sqlite3_result_error_nomem(LongTo_s3_context(context));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1error_1toobig(
    JNIEnv* env,
    jclass clazz,
    jlong context
) {
    sqlite3_result_error_toobig(LongTo_s3_context(context));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1int(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint value
) {
    sqlite3_result_int(LongTo_s3_context(context), value);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1int64(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jlong value
) {
    sqlite3_result_int64(LongTo_s3_context(context), value);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1null(
    JNIEnv* env,
    jclass clazz,
    jlong context
) {
    sqlite3_result_null(LongTo_s3_context(context));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1pointer(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jobject data,
    jstring type,
    jobject destructor
) {
    const auto pContext = LongTo_s3_context(context);
    const auto zType = JstringToUtf8(type);
    const auto freeable = AllocateFreeable(zType, data, destructor);
    const auto destroyer = FreeableDestroyer(freeable);

    sqlite3_result_pointer(pContext, freeable, zType, destroyer);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1subtype(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint subtype
) {
    sqlite3_result_subtype(LongTo_s3_context(context), subtype);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1text(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jstring value
) {
    const auto pContext = LongTo_s3_context(context);
    int bufferSize = 0;
    const auto buffer = JstringToUtf8Out(value, &bufferSize);
    const auto freeable = AllocateFreeablePointer(buffer, nullptr);
    const auto pDestructor = FreeableDestroyerPush(buffer, freeable);

    return sqlite3_result_text(pContext, buffer, bufferSize, pDestructor);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1text64(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jlong buffer,
    jlong size,
    jobject destructor,
    jint encoding
) {
    const auto pContext = LongTo_s3_context(context);
    const auto pBuffer = reinterpret_cast<char*>(LongToPtr(buffer));
    const auto freeable = AllocateFreeablePointer(nullptr, destructor);
    const auto destroyer = FreeableDestroyerPush(pBuffer, freeable);

    sqlite3_result_text64(pContext, pBuffer, size, destroyer, encoding);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1value(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jlong value
) {
    sqlite3_result_value(LongTo_s3_context(context), LongTo_s3_value(value));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1zeroblob(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint size
) {
    sqlite3_result_zeroblob(LongTo_s3_context(context), size);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1result_1zeroblob64(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jlong size
) {
    return sqlite3_result_zeroblob64(LongTo_s3_context(context), size);
}

/**
 * Calls the `RollbackHookCallback` hook.
 */
static void rollbackHookCaller(void* pDbStateHook) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    HookEnterDbState(rollbackHook);
    env->CallVoidMethod(instance, call);
    HookLeave();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1rollback_1hook(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceInstance(
        rollbackHook,
        "()V",
        "RollbackHookCallback",
        sqlite3_rollback_hook(pDb, rollbackHookCaller, pDbState),
        sqlite3_rollback_hook(pDb, nullptr, nullptr)
    );
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1serialize(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring schema,
    jobject outSize,
    jint flags
) {
    const auto zSchema = JstringToUtf8(schema);

    OutputPointerEnterInt64(outSize);
    const auto buffer = sqlite3_serialize(LongTo_s3(db), zSchema, outSize_, flags);
    const auto rc = (buffer == nullptr) ? SQLITE_NOMEM : SQLITE_OK;
    OutputPointerLeaveInt64(outSize);

    sqlite3_free(zSchema);

    return PtrToLong(buffer);
}

/**
 * Calls the `AuthorizerCallback` hook.
 */
static int authorizerHookCaller(
    void* pDbStateHook,
    int opId,
    const char* pString1,
    const char* pString2,
    const char* pString3,
    const char* pString4
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    const auto string1 = Utf8ToJstring(pString1);
    const auto string2 = Utf8ToJstring(pString2);
    const auto string3 = Utf8ToJstring(pString3);
    const auto string4 = Utf8ToJstring(pString4);

    HookEnterDbState(authorizer);
    jint result = env->CallIntMethod(instance, call, opId, string1, string2, string3, string4);
    HookLeave();

    LocalRefDestroy(string1);
    LocalRefDestroy(string2);
    LocalRefDestroy(string3);
    LocalRefDestroy(string4);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1set_1authorizer(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceResultCode(
        authorizer,
        "()I",
        "AuthorizerHookCallback",
        sqlite3_set_authorizer(pDb, authorizerHookCaller, pDbState),
        sqlite3_set_authorizer(pDb, nullptr, nullptr)
    );
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1set_1auxdata(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint index,
    jobject destructor
) {
    const auto pContext = LongTo_s3_context(context);
    const auto pointer = sqlite3_malloc(sizeof(void*));
    const auto freeable = AllocateFreeablePointer(pointer, destructor);
    const auto destroyer = FreeableDestroyerPush(pointer, freeable);

    sqlite3_set_auxdata(pContext, index, pointer, destroyer);
    return PtrToLong(pointer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1set_1errmsg(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint errorCode,
    jstring message
) {
    ReturnWithString(message, sqlite3_set_errmsg(LongTo_s3(db), errorCode, message_));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1set_1last_1insert_1rowid(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jlong rowId
) {
    sqlite3_set_last_insert_rowid(LongTo_s3(db), rowId);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1shutdown(
    JNIEnv* env,
    jclass clazz
) {
    return sqlite3_shutdown();
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1snapshot_1cmp(
    JNIEnv* env,
    jclass clazz,
    jlong snapshot1,
    jlong snapshot2
) {
    return sqlite3_snapshot_cmp(LongTo_s3_snapshot(snapshot1), LongTo_s3_snapshot(snapshot2));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1snapshot_1free(
    JNIEnv* env,
    jclass clazz,
    jlong snapshot
) {
    sqlite3_snapshot_free(LongTo_s3_snapshot(snapshot));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1snapshot_1get(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jobject outSnapshot
) {
    OutputPointerEnterPointer(sqlite3_snapshot*, outSnapshot);
    const auto zName = JstringToUtf8(name);
    const auto rc = sqlite3_snapshot_get(LongTo_s3(db), zName, outSnapshot_);
    OutputPointerLeavePointer(outSnapshot);

    sqlite3_free(zName);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1snapshot_1open(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jlong snapshot
) {
    ReturnWithString(
        name,
        sqlite3_snapshot_open(LongTo_s3(db), name_, LongTo_s3_snapshot(snapshot))
    );
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1snapshot_1recover(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name
) {
    ReturnWithString(name, sqlite3_snapshot_recover(LongTo_s3(db), name_));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1soft_1heap_1limit64(
    JNIEnv* env,
    jclass clazz,
    jlong limit
) {
    return sqlite3_soft_heap_limit64(limit);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1sourceid(
    JNIEnv* env,
    jclass clazz
) {
    return Utf8ToJstring(sqlite3_sourceid());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1sql(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return Utf8ToJstring(sqlite3_sql(LongTo_s3_stmt(stmt)));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1status(
    JNIEnv* env,
    jclass clazz,
    jint option,
    jobject outCurrent,
    jobject outHighwater,
    jint resetFlag
) {
    OutputPointerEnterInt32(outCurrent);
    OutputPointerEnterInt32(outHighwater);
    const auto rc = sqlite3_status(option, outCurrent_, outHighwater_, resetFlag);
    OutputPointerLeaveInt32(outCurrent);
    OutputPointerLeaveInt32(outHighwater);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1status64(
    JNIEnv* env,
    jclass clazz,
    jint option,
    jobject outCurrent,
    jobject outHighwater,
    jint resetFlag
) {
    OutputPointerEnterInt64(outCurrent);
    OutputPointerEnterInt64(outHighwater);
    const auto rc = sqlite3_status64(option, outCurrent_, outHighwater_, resetFlag);
    OutputPointerLeaveInt64(outCurrent);
    OutputPointerLeaveInt64(outHighwater);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1step(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_step(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1stmt_1busy(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_stmt_busy(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1stmt_1explain(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint mode
) {
    return sqlite3_stmt_explain(LongTo_s3_stmt(stmt), mode);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1stmt_1isexplain(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_stmt_isexplain(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1stmt_1readonly(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_stmt_readonly(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1stmt_1status(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint counter,
    jint resetFlag
) {
    return sqlite3_stmt_status(LongTo_s3_stmt(stmt), counter, resetFlag);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1strglob(
    JNIEnv* env,
    jclass clazz,
    jstring pattern,
    jstring input
) {
    ReturnWithStrings(pattern, input, sqlite3_strglob(pattern_, input_));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1stricmp(
    JNIEnv* env,
    jclass clazz,
    jstring first,
    jstring second
) {
    ReturnWithStrings(first, second, sqlite3_stricmp(first_, second_));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1strlike(
    JNIEnv* env,
    jclass clazz,
    jstring pattern,
    jstring input,
    jint escape
) {
    ReturnWithStrings(pattern, input, sqlite3_strlike(pattern_, input_, escape));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1strnicmp(
    JNIEnv* env,
    jclass clazz,
    jstring first,
    jstring second,
    jint maxChars
) {
    ReturnWithStrings(first, second, sqlite3_strnicmp(first_, second_, maxChars));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1system_1errno(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_system_errno(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1table_1column_1metadata(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring dbName,
    jstring tableName,
    jstring columnName,
    jobject outDataType,
    jobject outCollationSeqName,
    jobject outNotNull,
    jobject outPrimaryKey,
    jobject outAutoIncrement
) {
    const auto pDb = LongTo_s3(db);
    const auto zDbName = JstringToUtf8(dbName);
    const auto zTableName = JstringToUtf8(tableName);
    const auto zColumnName = JstringToUtf8(columnName);

    OutputPointerEnterStringConst(outDataType);
    OutputPointerEnterStringConst(outCollationSeqName);
    OutputPointerEnterInt32(outNotNull);
    OutputPointerEnterInt32(outPrimaryKey);
    OutputPointerEnterInt32(outAutoIncrement);

    const auto rc = sqlite3_table_column_metadata(
        pDb,
        zDbName,
        zTableName,
        zColumnName,
        outDataType_,
        outCollationSeqName_,
        outNotNull_,
        outPrimaryKey_,
        outAutoIncrement_
    );

    OutputPointerLeaveString(outDataType);
    OutputPointerLeaveString(outCollationSeqName);
    OutputPointerLeaveInt32(outNotNull);
    OutputPointerLeaveInt32(outPrimaryKey);
    OutputPointerLeaveInt32(outAutoIncrement);

    sqlite3_free(zDbName);
    sqlite3_free(zTableName);
    sqlite3_free(zColumnName);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1total_1changes(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_total_changes(LongTo_s3(db));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1total_1changes64(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_total_changes64(LongTo_s3(db));
}

/**
 * Calls the `TraceCallback` hook.
 */
static int traceCaller(
    unsigned int code,
    void* pDbStateHook,
    void* pP,
    void* pX
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    const auto pPointer = PtrToLong(pP);
    jobject xPointer = nullptr;

    switch (code) {
        case SQLITE_TRACE_STMT:
            xPointer = Utf8ToJstring(reinterpret_cast<char* >(pX));
            break;
        case SQLITE_TRACE_PROFILE:
            xPointer = PrimitiveBoxLong(*reinterpret_cast<sqlite3_int64*>(pX));
            break;
        case SQLITE_TRACE_ROW:
        case SQLITE_TRACE_CLOSE:
            break;
        default:
            FatalError(sqlite3_mprintf("Unexpected trace code %d", code));
    }

    HookEnterDbState(trace);

    const auto rc = env->CallIntMethod(
        instance,
        call,
        static_cast<jint>(code),
        pPointer,
        xPointer
    );

    HookLeave();
    LocalRefDestroy(xPointer);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1trace_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint mask,
    jobject callback
) {
    DbHookReplaceResultCode(
        trace,
        "(IJLjava/lang/Object;)I",
        "TraceCallback",
        sqlite3_trace_v2(pDb, mask, traceCaller, pDbState),
        sqlite3_trace_v2(pDb, 0, nullptr, nullptr)
    );
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1txn_1state(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring schema
) {
    ReturnWithString(schema, sqlite3_txn_state(LongTo_s3(db), schema_));
}

/**
 * Calls the `UpdateHookCallback` hook.
 */
static void updateHookCaller(
    void* pDbStateHook,
    int op,
    char const* zDb,
    char const* zName,
    sqlite3_int64 rowId
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    const auto dbName = Utf8ToJstring(zDb);
    const auto dbTable = Utf8ToJstring(zName);

    HookEnterDbState(updateHook);
    env->CallVoidMethod(instance, call, op, dbName, dbTable, rowId);
    HookLeave();

    LocalRefDestroy(dbName);
    LocalRefDestroy(dbTable);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1update_1hook(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceInstance(
        updateHook,
        "(ILjava/lang/String;Ljava/lang/String;J)V",
        "UpdateHookCallback",
        sqlite3_update_hook(pDb, updateHookCaller, pDbState),
        sqlite3_update_hook(pDb, nullptr, nullptr)
    );
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1uri_1boolean(
    JNIEnv* env,
    jclass clazz,
    jstring fileName,
    jstring parameter,
    jint def
) {
    ReturnWithStrings(fileName, parameter, sqlite3_uri_boolean(fileName_, parameter_, def));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1uri_1int64(
    JNIEnv* env,
    jclass clazz,
    jstring fileName,
    jstring parameter,
    jlong def
) {
    ReturnWithStrings(fileName, parameter, sqlite3_uri_int64(fileName_, parameter_, def));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1uri_1key(
    JNIEnv* env,
    jclass clazz,
    jstring fileName,
    jint index
) {
    ReturnWithString(fileName, Utf8ToJstring(sqlite3_uri_key(fileName_, index)));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1uri_1parameter(
    JNIEnv* env,
    jclass clazz,
    jstring fileName,
    jstring parameter
) {
    ReturnWithStrings(
        fileName,
        parameter,
        Utf8ToJstring(sqlite3_uri_parameter(fileName_, parameter_))
    );
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1user_1data(
    JNIEnv* env,
    jclass clazz,
    jlong context
) {
    FunctionPointerDeclare(Function, sqlite3_user_data(LongTo_s3_context(context)));
    return pFunction->appData;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1blob(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    const auto pValue = LongTo_s3_value(value);

    BufferBlobDeclare(
        jbyteArray,
        KJV.emptyByteArray,
        BufferToByteArray(pointer, length),
        sqlite3_value_blob(pValue),
        sqlite3_value_bytes(pValue),
        sqlite3_value_type(pValue)
    )

    return result;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1buffer(
    JNIEnv* env,
    jclass clazz,
    jlong value,
    jobject outSize
) {
    const auto pValue = LongTo_s3_value(value);

    BufferBlobDeclare(
        void *,
        KK.emptyBufferPointer,
        BufferToByteArray(pointer, length),
        sqlite3_value_blob(pValue),
        sqlite3_value_bytes(pValue),
        sqlite3_value_type(pValue),
        OutputPointerSetInt64Value(outSize, length);
    )

    return PtrToLong(result);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1bytes(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_bytes(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1double(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_double(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1dup(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return PtrToLong(sqlite3_value_dup(LongTo_s3_value(value)));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1encoding(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_encoding(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1free(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    sqlite3_value_free(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1frombind(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_frombind(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1int(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_int(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1int64(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_int64(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1nochange(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_nochange(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1numeric_1type(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_numeric_type(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1pointer(
    JNIEnv* env,
    jclass clazz,
    jlong value,
    jstring type
) {
    const auto zType = JstringToUtf8(type);
    const auto pointer = sqlite3_value_pointer(LongTo_s3_value(value), zType);

    sqlite3_free(zType);

    if (pointer == nullptr) {
        return nullptr;
    }

    const auto freeable = reinterpret_cast<Freeable*>(pointer);
    return freeable->data;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1subtype(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return static_cast<jint>(sqlite3_value_subtype(LongTo_s3_value(value)));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1text(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return Utf8ToJstring(sqlite3_value_text(LongTo_s3_value(value)));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1value_1type(
    JNIEnv* env,
    jclass clazz,
    jlong value
) {
    return sqlite3_value_type(LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vfs_1find(
    JNIEnv* env,
    jclass clazz,
    jstring name
) {
    ReturnWithString(name, PtrToLong(sqlite3_vfs_find(name_)));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vfs_1register(
    JNIEnv* env,
    jclass clazz,
    jlong vfs,
    jint makeDefault
) {
    return sqlite3_vfs_register(LongTo_s3_vfs(vfs), makeDefault);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vfs_1unregister(
    JNIEnv* env,
    jclass clazz,
    jlong vfs
) {
    return sqlite3_vfs_unregister(LongTo_s3_vfs(vfs));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1collation(
    JNIEnv* env,
    jclass clazz,
    jlong info,
    jint index
) {
    return Utf8ToJstring(sqlite3_vtab_collation(LongTo_s3_index_info(info), index));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1config(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint option,
    jobjectArray args
) {
    const auto pDb = LongTo_s3(db);

    switch (option) {
        // []
        case SQLITE_VTAB_DIRECTONLY:
        case SQLITE_VTAB_INNOCUOUS:
        case SQLITE_VTAB_USES_ALL_SCHEMAS: {
            return sqlite3_vtab_config(pDb, option);
        }
            // [Int]
        case SQLITE_VTAB_CONSTRAINT_SUPPORT: {
            ArrayLengthEnsure(args, 1);
            return sqlite3_vtab_config(pDb, option, ArrayIntGet(args, 0));
        }
        default:
            return SQLITE_MISUSE;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1distinct(
    JNIEnv* env,
    jclass clazz,
    jlong info
) {
    return sqlite3_vtab_distinct(LongTo_s3_index_info(info));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1in(
    JNIEnv* env,
    jclass clazz,
    jlong info,
    jint index,
    jint handle
) {
    return sqlite3_vtab_in(LongTo_s3_index_info(info), index, handle);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1in_1first(
    JNIEnv* env,
    jclass clazz,
    jlong value,
    jobject outValue
) {
    OutputPointerEnterPointer(sqlite3_value*, outValue);
    const auto rc = sqlite3_vtab_in_first(LongTo_s3_value(value), outValue_);
    OutputPointerLeavePointer(outValue);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1in_1next(
    JNIEnv* env,
    jclass clazz,
    jlong value,
    jobject outValue
) {
    OutputPointerEnterPointer(sqlite3_value*, outValue);
    const auto rc = sqlite3_vtab_in_next(LongTo_s3_value(value), outValue_);
    OutputPointerLeavePointer(outValue);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1nochange(
    JNIEnv* env,
    jclass clazz,
    jlong context
) {
    return sqlite3_vtab_nochange(LongTo_s3_context(context));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1on_1conflict(
    JNIEnv* env,
    jclass clazz,
    jlong db
) {
    return sqlite3_vtab_on_conflict(LongTo_s3(db));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1vtab_1rhs_1value(
    JNIEnv* env,
    jclass clazz,
    jlong info,
    jint index,
    jobject outValue
) {
    OutputPointerEnterPointer(sqlite3_value*, outValue);
    const auto rc = sqlite3_vtab_rhs_value(LongTo_s3_index_info(info), index, outValue_);
    OutputPointerLeavePointer(outValue);

    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1wal_1autocheckpoint(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jint nFrame
) {
    return sqlite3_wal_autocheckpoint(LongTo_s3(db), nFrame);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1wal_1checkpoint(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name
) {
    ReturnWithString(name, sqlite3_wal_checkpoint(LongTo_s3(db), name_));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1wal_1checkpoint_1v2(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jstring name,
    jint mode,
    jobject outNlog,
    jobject outCckpt
) {
    const auto zName = JstringToUtf8(name);

    OutputPointerEnterInt32(outNlog);
    OutputPointerEnterInt32(outCckpt);

    const auto rc = sqlite3_wal_checkpoint_v2(LongTo_s3(db), zName, mode, outNlog_, outCckpt_);

    OutputPointerLeaveInt32(outNlog);
    OutputPointerLeaveInt32(outCckpt);
    sqlite3_free(zName);

    return rc;
}

/**
 * Calls the `WalHookCallback` hook.
 */
static int walHookCaller(
    void* pDbStateHook,
    sqlite3* pDb,
    const char* zDb,
    int nPage
) {
    JniEnvDeclare();
    DbStateDeclareDirect(pDbStateHook);

    const auto db = PtrToLong(pDb);
    const auto dbName = Utf8ToJstring(zDb);

    HookEnterDbState(walHook);
    const auto rc = env->CallIntMethod(instance, call, db, dbName, nPage);
    HookLeave();

    LocalRefDestroy(dbName);
    return rc;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1wal_1hook(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback
) {
    DbHookReplaceInstance(
        walHook,
        "(JLjava/lang/String;I)I",
        "WalHookCallback",
        sqlite3_wal_hook(pDb, walHookCaller, pDbState),
        sqlite3_wal_hook(pDb, nullptr, nullptr)
    );
}