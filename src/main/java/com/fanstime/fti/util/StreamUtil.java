package com.fanstime.fti.util;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Bynum Williams on 13.01.18.
 */
public class StreamUtil {

    /**
     * Stream or value or empty stream if value is null.
     */
    public static <T> Stream<T> streamOf(T value) {
        return Optional.ofNullable(value)
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
