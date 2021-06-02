package ru.zuma.endpoint;

import java.util.Objects;

public abstract class RestEndpointBase implements RestEndpoint {
    private String path;

    public RestEndpointBase(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestEndpointBase that)) return false;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
