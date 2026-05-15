#include <jni.h>
#include <ksqlite.h>

#ifndef KSQLITE_JNI
#define KSQLITE_JNI

// TODO move the following at the end of the when all the functions are defined
#endif //KSQLITE_JNI

///////////////////////////////////////////////////////////////////////////
// Errors
///////////////////////////////////////////////////////////////////////////

/**
 * Fails fatally with an out of memory message.
 */
static inline void raiseOutOfMemory(JNIEnv* const env) {
    env->FatalError("KSQLite JNI is out of memory.");
}

/**
 * Raises a fatal error if expression E is false.
 */
#define OutOfMemoryCheck(E) if (!(E)) raiseOutOfMemory(env)

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

    // Auto extension
    struct V {
        sqlite3_mutex* mutex;
        jobject callback;
    } autoExtension;
};

static KsqliteJniGlobal KsqliteJniGlobalState;

#define KJG KsqliteJniGlobalState

///////////////////////////////////////////////////////////////////////////
// JNI hooks
///////////////////////////////////////////////////////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    KJG.jvm = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM* vm, void* reserved) {
    sqlite3_mutex_free(KJG.autoExtension.mutex);

    KJG.jvm = nullptr;
}

///////////////////////////////////////////////////////////////////////////
// Ksqlite Init
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes the global state.
 * Initialization is done here because the JNIEnv is required which is not the case in JNI_OnLoad().
 */
extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1init(
    JNIEnv* env,
    jclass clazz
) {
    sqlite3_initialize();

    // Hooks
    KJG.autoExtension.mutex = sqlite3_mutex_alloc(SQLITE_MUTEX_FAST);
    OutOfMemoryCheck(KJG.autoExtension.mutex);

    sqlite3_shutdown();
}

///////////////////////////////////////////////////////////////////////////
// SQLite Hooks
///////////////////////////////////////////////////////////////////////////

/**
 * Handle auto-extension globally.
 */
static void autoExtensionHandler(
    sqlite3* pDb,
    const char** pzErr,
    sqlite3_api_routines* ignored
) {
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
        rc = ksqlite_auto_extension(reinterpret_cast<xEntryPoint>(autoExtensionHandler));

        if (rc == SQLITE_OK) {
            const auto klass = env->GetObjectClass(callback);
            const auto handleMethodId = env->GetMethodID(klass, "call", "(JJ)I");

            LocalRefDestroy(klass);
            KJG.autoExtension.callback = GlobalRefCreate(callback);
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