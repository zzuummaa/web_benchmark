cmake_minimum_required(VERSION 3.0)
project(restinio_benchmark)

set(CMAKE_CXX_STANDARD 17)

if (CMAKE_BUILD_TYPE MATCHES Debug)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os -g3")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os -g3")
else()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fPIC")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -fPIC")
endif()

find_package(fmt REQUIRED)
find_package(unofficial-http-parser REQUIRED)
find_package(restinio REQUIRED)
find_package(json-dto REQUIRED)
include(cmake/asio-targets.cmake)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -static")

add_executable(single_handler single_handler_bench.cpp dto.h json_converter.h)
target_link_libraries(single_handler restinio::restinio asio json-dto::json-dto)

if (MSYS OR MINGW)
    set_target_properties(single_handler PROPERTIES COMPILE_FLAGS "-static")

    # Workaround linker bug
    target_link_options(single_handler PUBLIC -Wl,--default-image-base-low)
endif ()
