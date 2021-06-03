package ru.zuma.storage;

import ru.zuma.model.Snippet;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MapSnippetService implements SnippetService {
    private final Map<Integer, String> snippets;

    public MapSnippetService() {
        this.snippets = IntStream
            .range(0, 10_000)
            .mapToObj((i) -> Map.entry(i, "map snippet test id " + i))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean add(Snippet snippet) {
        return snippets.put(snippet.getId(), snippet.getSnippet()) == null;
    }

    @Override
    public Optional<String> get(Integer id) {
        return Optional.ofNullable(snippets.get(id));
    }
}
