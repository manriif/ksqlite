cmake_minimum_required(VERSION 3.22.1)
project("ksqlite-jni")

set(KSQLITE_JNI_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/ksqlite_jni_build")
add_subdirectory(${SQLITE_CMAKE_DIR} ${KSQLITE_JNI_BUILD_DIR})

add_library(${KSQLITE_LIB_NAME} SHARED
        ksqlite-jni.cpp
)

target_link_libraries(${KSQLITE_LIB_NAME} PRIVATE
        "-Wl,--whole-archive"
        ${SQLITE_LIB_NAME}
        "-Wl,--no-whole-archive"
)