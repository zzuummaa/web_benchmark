//
// Created by Stepan on 31.05.2021.
//

#ifndef RESTINIO_BENCHMARK_SNIPPET_SERVICE_H
#define RESTINIO_BENCHMARK_SNIPPET_SERVICE_H

#include <unordered_map>
#include <string>
#include <optional>
#include <mutex>

#include "dto.h"

struct map_snippet_service_t {
    map_snippet_service_t() {
        std::lock_guard lc(m);
        for (size_t id_counter = 0; id_counter < 10'000; id_counter++) {
            snippets.insert(std::make_pair(id_counter, "map snippet test id " + std::to_string(id_counter)));
        }
    }

    std::optional<std::string> get(size_t id) {
        std::lock_guard lc(m);
        auto it = snippets.find(id);
        return it == snippets.end() ? std::nullopt : std::make_optional(it->second);
    }

    std::optional<bool> add(const snippet_record_t& record) {
        return add(record.id, std::string(record.snippet));
    }

    std::optional<bool> add(snippet_record_t&& record) {
        return add(record.id, std::move(record.snippet));
    }

private:
    // TODO use type as template parameter for single thread mocks
    std::mutex m;
    std::unordered_map<size_t, std::string> snippets;

    std::optional<bool> add(size_t id, std::string&& snippet) {
        std::lock_guard lc(m);
        return snippets.insert_or_assign(id, snippet).second;
    }
};

#endif //RESTINIO_BENCHMARK_SNIPPET_SERVICE_H
