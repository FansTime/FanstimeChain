package com.fanstime.fti.datasource;

/**
 * Interface for estimating size of a specific Java type
 *
 * Created by Jay Nicolas on 01.05.2018.
 */
public interface MemSizeEstimator<E> {

    long estimateSize(E e);

    /**
     * byte[] type size estimator
     */
    MemSizeEstimator<byte[]> ByteArrayEstimator = bytes -> {
        return bytes == null ? 0 : bytes.length + 16; // 4 - compressed ref size, 12 - Object header
    };


}
