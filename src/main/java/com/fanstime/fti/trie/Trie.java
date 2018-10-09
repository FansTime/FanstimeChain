package com.fanstime.fti.trie;

import com.fanstime.fti.datasource.Source;

/**
 * Created by Tony Hunt on 05.03.2018.
 */
public interface Trie<V> extends Source<byte[], V> {

    byte[] getRootHash();

    void setRoot(byte[] root);

    /**
     * Recursively delete all nodes from root
     */
    void clear();
}
