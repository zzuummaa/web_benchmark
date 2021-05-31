cmake_minimum_required(VERSION 3.0)
project(restinio_benchmark)

set(CMAKE_CXX_STANDARD 17)

find_package(fmt REQUIRED)
find_package(unofficial-http-parser REQUIRED)
find_package(restinio REQUIRED)
include(cmake/asio-targets.cmake)

add_executable(single_handler single_handler_bench.cpp)
target_link_libraries(single_handler restinio::restinio asio)

if (MSYS OR MINGW)
    # Workaround linker bug
    target_link_options(single_handler PUBLIC -Wl,--default-image-base-low)
endif ()
