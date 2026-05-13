#include <jni.h>
#include <ksqlite.h>

extern "C" JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_ksqliteHello(
    JNIEnv* env,
    jclass clazz
) {
    const auto hello = "Hello from C++, Ksqlite";
    return env->NewStringUTF(hello);
}