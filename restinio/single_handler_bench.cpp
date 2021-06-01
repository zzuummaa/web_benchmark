/*
	restinio bench single handler.
*/
#include <stdexcept>
#include <iostream>
#include <fstream>

#include <restinio/all.hpp>

#include "app_args.hpp"
#include "server_settings_traits.h"
#include "snippet_service.h"
#include "request_handler.h"
#include "json_converter.h"

using default_req_handler = req_handler_t<map_snippet_service_t, json_body_converter_t>;

template< typename Settings >
void
setup_common_values(
	const app_args_t & args,
	Settings & settings )
{
	using namespace std::chrono;

	settings
		.address( args.m_address )
		.port( args.m_port )
		.buffer_size( 1024u )
		.read_next_http_message_timelimit( 5s )
		.write_http_response_timelimit( 5s )
		.handle_request_timeout( 5s )
		.max_pipelined_requests( 4u );
}

template< bool Use_Connection_Limits >
struct settings_tunner;

template<>
struct settings_tunner< false >
{
	template< typename Settings >
	static void
	tune( const app_args_t & args, Settings & settings )
	{
		setup_common_values( args, settings );
	}
};

template<>
struct settings_tunner< true >
{
	template< typename Settings >
	static void
	tune( const app_args_t & args, Settings & settings )
	{
		setup_common_values( args, settings );
		settings.max_parallel_connections( args.m_max_parallel_connections );

		std::cout << "connection_count_limit: " <<
				args.m_max_parallel_connections << std::endl;
	}
};

template < typename Traits >
void run_app( const app_args_t args )
{
	auto settings = restinio::on_thread_pool< Traits >( args.m_pool_size );
	settings_tunner< Traits::use_connection_count_limiter >::tune(
			args, settings );

	restinio::run( std::move(settings) );
}

int main(int argc, const char *argv[]) {
    try {
        const auto args = app_args_t::parse(argc, argv);

        if (!args.m_help) {
            std::cout << "pool size: " << args.m_pool_size << std::endl;

            if (1 < args.m_pool_size) {
                if (0u == args.m_max_parallel_connections) {
                    if (args.m_trace_server) {
                        run_app<multi_thread_no_limit_with_logger_traits_t<default_req_handler>>(args);
                    } else {
                        run_app<multi_thread_no_limit_no_logger_traits_t<default_req_handler>>(args);
                    }
                } else {
                    if (args.m_trace_server) {
                        run_app<multi_thread_with_limit_with_logger_traits_t<default_req_handler>>(args);
                    } else {
                        run_app<multi_thread_with_limit_no_logger_traits_t<default_req_handler>>(args);
                    }
                }
            } else if (1 == args.m_pool_size) {
                if (0u == args.m_max_parallel_connections) {
                    if (args.m_trace_server) {
                        run_app<single_thread_no_limit_with_logger_traits_t<default_req_handler>>(args);
                    } else {
                        run_app<single_thread_no_limit_no_logger_traits_t<default_req_handler>>(args);
                    }
                } else {
                    if (args.m_trace_server) {
                        run_app<single_thread_with_limit_with_logger_traits_t<default_req_handler>>(args);
                    } else {
                        run_app<single_thread_with_limit_no_logger_traits_t<default_req_handler>>(args);
                    }
                }
            } else {
                throw std::runtime_error{"invalid asio pool size"};
            }
        }
    }
    catch (const std::exception &ex) {
        std::cerr << "Error: " << ex.what() << std::endl;
        return 1;
    }

    return 0;
}
