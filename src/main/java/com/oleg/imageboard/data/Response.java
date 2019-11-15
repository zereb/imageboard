package com.oleg.imageboard.data;

import org.jetbrains.annotations.NotNull;

public class Response {
    public final String error;
    public final Object response;

    public Response(@NotNull String error, @NotNull Object object) {
        this.error = error;
        this.response = object;
    }
}