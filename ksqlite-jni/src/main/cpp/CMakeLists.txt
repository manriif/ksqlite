cmake_minimum_required(VERSION 3.22.1)
project("ksqlite-jni")

set(KSQLITE_JNI_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/ksqlite_jni_build")
add_subdirectory(${KSQLITE_CMAKE_DIR} ${KSQLITE_JNI_BUILD_DIR})
add_library(${CMAKE_PROJECT_NAME} SHARED ksqlite-jni.cpp)
target_link_libraries(${CMAKE_PROJECT_NAME} ${KSQLITE_LIB_NAME})