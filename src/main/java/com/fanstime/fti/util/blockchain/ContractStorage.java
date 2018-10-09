package com.fanstime.fti.util.blockchain;

/**
 * Represents the contract storage which is effectively the
 * mapping( uint256 => uint256 )
 *
 * Created by Jay Nicolas on 23.03.2018.
 */
public interface ContractStorage {
    byte[] getStorageSlot(long slot);
    byte[] getStorageSlot(byte[] slot);
}
