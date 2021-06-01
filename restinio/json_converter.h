//
// Created by Stepan on 01.06.2021.
//

#ifndef RESTINIO_BENCHMARK_JSON_CONVERTER_H
#define RESTINIO_BENCHMARK_JSON_CONVERTER_H

#include <json_dto/pub.hpp>

#include "dto.h"

struct json_invalid_representable_t {};

template<typename DTO>
struct json_representable_t: public DTO, json_invalid_representable_t {

};

template<>
struct json_representable_t<snippet_record_t>: public snippet_record_t {
    template<typename JSON_IO>
    void json_io(JSON_IO &io) {
        io
            & json_dto::mandatory("id", id)
            & json_dto::mandatory("snippet", snippet);
    }
};

struct json_body_converter_t {
    template<typename DTO>
    std::optional<DTO> to_dto(const std::string& json) {
        static_assert(!std::is_base_of_v<json_invalid_representable_t, json_representable_t<DTO>>, "JSON conversion not implemented for this type");

        try {
            auto dto = json_dto::from_json<json_representable_t<DTO>>(json);
            return std::make_optional(static_cast<DTO&>(dto));
        } catch (const std::exception& e) {
            return std::nullopt;
        }
    }

};

#endif //RESTINIO_BENCHMARK_JSON_CONVERTER_H
