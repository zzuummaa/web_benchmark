cmake_minimum_required(VERSION 3.19)
project(restinio_benchmark)

set(CMAKE_CXX_STANDARD 17)

find_package(fmt REQUIRED)
find_package(unofficial-http-parser REQUIRED)
find_package(restinio REQUIRED)
include(cmake/asio-targets.cmake)

add_executable(restinio_benchmark main.cpp)
target_link_libraries(restinio_benchmark restinio::restinio asio)