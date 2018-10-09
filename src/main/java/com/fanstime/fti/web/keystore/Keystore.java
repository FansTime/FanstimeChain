package com.fanstime.fti.web.keystore;

import com.fanstime.fti.crypto.ECKey;

/**
 * Created by Bynum Williams on 01.08.16.
 *
 * Each method could throw {RuntimeException}, because of access to IO and crypto functions.
 */
public interface Keystore {

    void removeKey(String address);

    void storeKey(ECKey key, String password) throws RuntimeException;

    void storeRawKeystore(String content, String address) throws RuntimeException;

    String[] listStoredKeys();

    ECKey loadStoredKey(String address, String password) throws RuntimeException;

    /**
     * Check if keystore has file with key for passed address.
     * @param address - 40 chars
     * @return
     */
    boolean hasStoredKey(String address);
}
