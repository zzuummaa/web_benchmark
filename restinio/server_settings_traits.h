//
// Created by Stepan on 31.05.2021.
//

#ifndef RESTINIO_BENCHMARK_SERVER_SETTINGS_TRAITS_H
#define RESTINIO_BENCHMARK_SERVER_SETTINGS_TRAITS_H

#include <restinio/traits.hpp>

template<typename Request_Handler>
struct multi_thread_no_limit_no_logger_traits_t : public restinio::traits_t<
        restinio::asio_timer_manager_t,
        restinio::null_logger_t,
        Request_Handler >
{};

template<typename Request_Handler>
struct multi_thread_no_limit_with_logger_traits_t : public restinio::traits_t<
        restinio::asio_timer_manager_t,
        restinio::shared_ostream_logger_t,
        Request_Handler >
{};

template<typename Request_Handler>
struct multi_thread_with_limit_no_logger_traits_t : public restinio::traits_t<
        restinio::asio_timer_manager_t,
        restinio::null_logger_t,
        Request_Handler >
{
    static constexpr bool use_connection_count_limiter = true;
};

template<typename Request_Handler>
struct multi_thread_with_limit_with_logger_traits_t : public restinio::traits_t<
        restinio::asio_timer_manager_t,
        restinio::shared_ostream_logger_t,
        Request_Handler >
{
    static constexpr bool use_connection_count_limiter = true;
};

template<typename Request_Handler>
struct single_thread_no_limit_no_logger_traits_t : public restinio::single_thread_traits_t<
        restinio::asio_timer_manager_t,
        restinio::null_logger_t,
        Request_Handler >
{};

template<typename Request_Handler>
struct single_thread_no_limit_with_logger_traits_t : public restinio::single_thread_traits_t<
        restinio::asio_timer_manager_t,
        restinio::single_threaded_ostream_logger_t,
        Request_Handler >
{};

template<typename Request_Handler>
struct single_thread_with_limit_no_logger_traits_t : public restinio::single_thread_traits_t<
        restinio::asio_timer_manager_t,
        restinio::null_logger_t,
        Request_Handler >
{
    static constexpr bool use_connection_count_limiter = true;
};

template<typename Request_Handler>
struct single_thread_with_limit_with_logger_traits_t : public restinio::single_thread_traits_t<
        restinio::asio_timer_manager_t,
        restinio::single_threaded_ostream_logger_t,
        Request_Handler >
{
    static constexpr bool use_connection_count_limiter = true;
};

#endif //RESTINIO_BENCHMARK_SERVER_SETTINGS_TRAITS_H
