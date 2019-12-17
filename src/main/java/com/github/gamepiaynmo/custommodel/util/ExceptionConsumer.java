package com.github.gamepiaynmo.custommodel.util;

import java.util.function.Consumer;

public interface ExceptionConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    static <T> Consumer<T> wrapper(ExceptionConsumer<T, Exception> throwingConsumer) {
        return i -> {
            try {
                throwingConsumer.accept(i);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
