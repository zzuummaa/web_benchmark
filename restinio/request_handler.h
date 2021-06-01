//
// Created by Stepan on 31.05.2021.
//

#ifndef RESTINIO_BENCHMARK_REQUEST_HANDLER_H
#define RESTINIO_BENCHMARK_REQUEST_HANDLER_H

#include <type_traits>
#include <restinio/all.hpp>
#include <cstdlib>

#include "dto.h"

const std::string resp_body{ "Hello world!" };

template<typename ResponseBuilder,
         typename = typename std::enable_if<std::is_move_constructible<ResponseBuilder>::value>::type>
ResponseBuilder&& add_default_headers(ResponseBuilder&& builder) {
    return std::move(builder
        .append_header("Server", "RESTinio Benchmark"));
}

template<typename SnippetService,
         typename BodyConverter>
struct req_handler_t {
    auto operator()(restinio::request_handle_t req) {
        if (req->header().path() == "/"
         || req->header().path() == "/index") {
            return handle_index_request(req);
        } else if (req->header().path() == "/snippet") {
            return handle_snippet_request(req);
        }

        return add_default_headers(req->create_response(restinio::status_not_found()))
            .done();
    }

    auto handle_index_request(const restinio::request_handle_t& req) {
        if (restinio::http_method_get() == req->header().method()) {
            return add_default_headers(req->create_response())
                        // .append_header_date_field()
                .append_header("Content-Type", "text/plain; charset=utf-8")
                .set_body(resp_body)
                .done();
        }
        return add_default_headers(req->create_response(restinio::status_not_found()))
            .done();
    }

    auto handle_snippet_request(const restinio::request_handle_t& req) {
        if (restinio::http_method_get() == req->header().method()) {
            const auto qp = restinio::parse_query( req->header().query() );

            if (qp.empty()) {
                // TODO get all snippets
                return add_default_headers(req->create_response(restinio::status_not_implemented()))
                    .done();
            }

            auto id_opt = qp.get_param("id");
            if (id_opt.has_value()) {
                // Handle GET request with parameter 'id'

                auto& id = id_opt.value();

                auto id_end = const_cast<char*>(id.data() + id.size());
                auto id_uint = strtoull(id.data(), &id_end, 10);
                if (id_uint == 0 && id[0] != '0') {
                    return add_default_headers(req->create_response(restinio::status_bad_request()))
                        .done();
                }

                auto snippet = snippet_service.get(id_uint);
                if (!snippet.has_value()) {
                    // TODO implement GET all logic
                    return add_default_headers(req->create_response(restinio::status_not_found()))
                        .append_header("Content-Type", "application/json; charset=utf-8")
                        .set_body("{ error: \"snippet not found\"}")
                        .done();
                }

                return add_default_headers(req->create_response())
                    // .append_header_date_field()
                    .append_header("Content-Type", "application/json; charset=utf-8")
                    .set_body("{ id: " + std::to_string(id_uint) + ", snippet: \"" + snippet.value() + "\"}")
                    .done();
            }

        } else if (restinio::http_method_post() == req->header().method()) {
            auto dto_opt = body_converter.template to_dto<snippet_record_t>(req->body());
            if (!dto_opt.has_value()) {
                return add_default_headers(req->create_response(restinio::status_bad_request()))
                    .done();
            }

            auto add_result = snippet_service.add(dto_opt.value());
            if (!add_result.has_value()) {
                return add_default_headers(req->create_response(restinio::status_bad_request()))
                    .done();
            }

            return add_default_headers(req->create_response())
                .done();
        }
        return add_default_headers(req->create_response(restinio::status_not_found()))
            .done();
    }

private:
    SnippetService snippet_service;
    BodyConverter body_converter;
};

#endif //RESTINIO_BENCHMARK_REQUEST_HANDLER_H
