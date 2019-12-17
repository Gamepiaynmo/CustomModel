package com.github.gamepiaynmo.custommodel.util;

import java.util.function.BiConsumer;

public interface ExceptionBiConsumer<K, V, E extends Exception> {
    void accept(K k, V v) throws E;

    static <K, V> BiConsumer<K, V> wrapper(ExceptionBiConsumer<K, V, Exception> throwingConsumer) {
        return (i, j) -> {
            try {
                throwingConsumer.accept(i, j);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
