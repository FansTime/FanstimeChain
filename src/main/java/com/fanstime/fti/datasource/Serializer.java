package com.fanstime.fti.datasource;

/**
 * Converter from one type to another and vice versa
 *
 * Created by Jay Nicolas on 17.03.2018.
 */
public interface Serializer<T, S> {
    /**
     * Converts T ==> S
     * Should correctly handle null parameter
     */
    S serialize(T object);
    /**
     * Converts S ==> T
     * Should correctly handle null parameter
     */
    T deserialize(S stream);
}
