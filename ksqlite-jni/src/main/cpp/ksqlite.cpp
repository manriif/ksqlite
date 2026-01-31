#include <jni.h>
#include <string>
#include <sqlite3mc_amalgamation.h>

extern "C" JNIEXPORT jstring JNICALL
Java_ksqlite_MainActivity_stringFromJNI(
    JNIEnv* env,
    jobject /* this */
) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}