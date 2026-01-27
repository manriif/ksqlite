cmake_minimum_required(VERSION 3.22.1)
project("komogen-sqlite")

add_library(${CMAKE_PROJECT_NAME} SHARED komogen-sqlite.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME} android log)