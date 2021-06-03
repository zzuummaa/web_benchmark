package ru.zuma.storage;

import ru.zuma.model.Snippet;

import java.util.Optional;

public interface SnippetService {
    boolean add(Snippet snippet);
    Optional<String> get(Integer id);
}
