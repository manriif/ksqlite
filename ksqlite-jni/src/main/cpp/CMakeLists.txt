cmake_minimum_required(VERSION 3.22.1)
project("ksqlite")

set(SQLITE_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/sqlite_build")
add_subdirectory(${SQLITE_CMAKE_DIR} ${SQLITE_BUILD_DIR})
add_library(${CMAKE_PROJECT_NAME} SHARED ksqlite.cpp)
target_link_libraries(${CMAKE_PROJECT_NAME} sqlite)