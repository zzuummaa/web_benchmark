package ru.zuma.model;

public class Snippet {
    private Integer id;
    private String snippet;

    public Snippet() {
    }

    public Snippet(int id, String snippet) {
        this.id = id;
        this.snippet = snippet;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
