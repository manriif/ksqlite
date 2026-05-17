#include <cstdlib>
#include <jni.h>

#include "ksqlite.h"

#ifndef KSQLITE_JNI
#define KSQLITE_JNI

// TODO move the following at the end of the when all the functions are defined
#endif //KSQLITE_JNI

///////////////////////////////////////////////////////////////////////////
// Classes
///////////////////////////////////////////////////////////////////////////

#define JAVA_STRING "java.lang.String"
#define KSQLITE_JNI_EXCEPTION "KsqliteJniException"
#define AUTO_EXTENSION_CALLBACK "AutoExtensionCallback"

///////////////////////////////////////////////////////////////////////////
// Exceptions
///////////////////////////////////////////////////////////////////////////

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
        env->FatalError(errorMessage);
    }
}

#define ExceptionClearAndAbort(M) exceptionClearAndAbort(env, (M))

/**
 * Raises a fatal error when an SQLite operation failed due to a lack of memory.
 */
static inline void raiseOutOfMemory(JNIEnv* const env) {
    env->FatalError("KSQLite JNI is out of memory.");
}

/**
 * Raises a fatal error if expression E is false.
 */
#define OutOfMemoryCheck(E) if (!(E)) raiseOutOfMemory(env)

static inline jclass classOrThrow(
    JNIEnv* const env,
    const char* name,
    const char* errorMessage
) {
    const auto klass = env->FindClass(name);
    ExceptionClearAndAbort(errorMessage);
    return klass;
}

#define RequireClass(klassName) classOrThrow(env, klassName, "Class " klassName " was not found")
#define RequireKsqliteClass(klassName) RequireClass("ksqlite." klassName)

/**
 * Raises a fatal error when a method was not found on a given class.
 */
static inline jmethodID methodIdOrThrow(
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

#define RequireMethod(klass, name, signature, className) \
    methodIdOrThrow(env, klass, name, signature,  "Method " className "#" name "() was not found")

#define RequireKsqliteMethod(klass, name, signature, className) \
    RequireMethod(klass, name, signature, "ksqlite." className)

/**
 * Raises a fatal error when a field was not found on a given class.
 */
static inline jfieldID fieldIdOrThrow(
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

#define RequireField(klass, name, signature, className) \
    fieldIdOrThrow(env, klass, name, signature,  "Field " className "#" name " was not found")

#define RequireKsqliteField(klass, name, signature, className) \
    RequireField(klass, name, signature, "ksqlite." className)

/**
 * Raises a fatal error if object is null.
 */
static inline jobject requireNonNull(
    JNIEnv* const env,
    jobject object
) {
    if (object == nullptr) {
        env->FatalError("Expected Java object not to be null");
    }

    return object;
}

#define RequireNonNull(O) requireNonNull(env, (O))

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
// Casting
///////////////////////////////////////////////////////////////////////////

#define PtrToLong(P) (jlong)((intptr_t)(P))
#define LongToPtr(L) (void*)((intptr_t)(L))

#define LongCast(T, L) (T*)((intptr_t)((L)))
#define LongCast_s3(L) LongCast(sqlite3, (L))
#define LongCast_s3_backup(L) LongCast(sqlite3_backup, (L))
#define LongCast_s3_blob(L) LongCast(sqlite3_blob, (L))
#define LongCast_s3_context(L) LongCast(sqlite3_context, (L))
#define LongCast_s3_stmt(L) LongCast(sqlite3_stmt, (L))
#define LongCast_s3_value(L) LongCast(sqlite3_value, (L))

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/*
** Global state, e.g. caches and metrics.
*/
struct KsqliteJniGlobal {
    JavaVM* jvm;

    // KsqliteJniException
    struct {
        jclass klass;
        jmethodID resultCode;
        jmethodID message;
    } ksqliteJniException;

    // Auto extension
    struct {
        sqlite3_mutex* mutex;
        jobject callback;
        jmethodID call;
    } autoExtension;
};

static KsqliteJniGlobal KsqliteJniGlobalState;
#define KJG KsqliteJniGlobalState

/**
 * Initializes and cache KsqliteJniException class and methods.
 */
static void initializeKsqliteJniException(JNIEnv* env) {
    KJG.ksqliteJniException.klass = RequireKsqliteClass(KSQLITE_JNI_EXCEPTION);

    KJG.ksqliteJniException.resultCode = RequireKsqliteMethod(
        KJG.ksqliteJniException.klass,
        "getResultCode",
        "I",
        KSQLITE_JNI_EXCEPTION
    );

    KJG.ksqliteJniException.message = RequireKsqliteMethod(
        KJG.ksqliteJniException.klass,
        "getMessage",
        JAVA_STRING,
        KSQLITE_JNI_EXCEPTION
    );
}

/**
 * Initializes and cache mutexes.
 */
static void initializeMutexes(JNIEnv* env) {
    // Auto extension
    KJG.autoExtension.mutex = sqlite3_mutex_alloc(SQLITE_MUTEX_FAST);
    OutOfMemoryCheck(KJG.autoExtension.mutex);
}

///////////////////////////////////////////////////////////////////////////
// JNI Lifecycle
///////////////////////////////////////////////////////////////////////////

#define JNI_VERSION_1_8 0x00010008
#define JNI_VERSION JNI_VERSION_1_8

/**
 * Returns the current JNIEnv object or abort if it cannot find the object.
 */
static JNIEnv* retrieveJniEnv() {
    JNIEnv* env = nullptr;

    if (KJG.jvm->GetEnv(reinterpret_cast<void**>(env), JNI_VERSION)) {
        fprintf(stderr, "Fatal error: cannot get current JNIEnv.\n");
        abort();
    }

    return env;
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    KJG.jvm = vm;
    const auto env = retrieveJniEnv();

    // Global
    initializeKsqliteJniException(env);

    // Sqlite3
    sqlite3_initialize();
    initializeMutexes(env);
    sqlite3_shutdown();

    return JNI_VERSION;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM* vm, void* reserved) {
    sqlite3_mutex_free(KJG.autoExtension.mutex);
    KJG.jvm = nullptr;
}

///////////////////////////////////////////////////////////////////////////
// SQLite Hooks
///////////////////////////////////////////////////////////////////////////

/**
 * Handle auto-extension globally.
 */
static int autoExtensionHandler(
    sqlite3* pDb,
    char** pzErr,
    const sqlite3_api_routines* pApi
) {
    const auto env = retrieveJniEnv();

    sqlite3_mutex_enter(KJG.autoExtension.mutex);

    const auto callback = RequireNonNull(KJG.autoExtension.callback);
    const auto call = KJG.autoExtension.call;

    sqlite3_mutex_leave(KJG.autoExtension.mutex);

    const auto dbPtr = PtrToLong(pDb);
    const auto apiPtr = PtrToLong(pApi);

    auto rc = env->CallIntMethod(callback, call, dbPtr, apiPtr);

    if (const auto exception = env->ExceptionOccurred()) {
        env->ExceptionClear();
        // TODO
        LocalRefDestroy(exception);
    }

    return rc;
}

///////////////////////////////////////////////////////////////////////////
// SQLite 1 to 1 mapping
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

    sqlite3_mutex_enter(KJG.autoExtension.mutex);

    if (KJG.autoExtension.callback != nullptr) {
        if (!env->IsSameObject(callback, KJG.autoExtension.callback)) {
            rc = SQLITE_MISUSE;
        }
    } else {
        rc = ksqlite_auto_extension(autoExtensionHandler);

        if (rc == SQLITE_OK) {
            const auto klass = env->GetObjectClass(callback);

            KJG.autoExtension.call =
                RequireKsqliteMethod(klass, "call", "(JJ)I", AUTO_EXTENSION_CALLBACK);

            KJG.autoExtension.callback = GlobalRefCreate(callback);
            LocalRefDestroy(klass);
        }
    }

    sqlite3_mutex_leave(KJG.autoExtension.mutex);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1cancel_1auto_1extension(
    JNIEnv* env,
    jclass clazz,
    jobject callback
) {
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1aggregate_1context(
    JNIEnv* env,
    jclass clazz,
    jlong p0,
    jint p1
) {
    const auto s3Context = LongCast_s3_context(p0);
    const auto pointer = sqlite3_aggregate_context(s3Context, p1);

    return PtrToLong(pointer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1autovacuum_1pages(
    JNIEnv* env,
    jclass clazz,
    jlong p0,
    jlong p1,
    jlong p2,
    jlong p3
) {
    // TODO: implement sqlite3_autovacuum_pages()
}