package com.fanstime.fti.util.blockchain;

import com.fanstime.fti.core.Block;

/**
 * Interface to Fti contract compiled with Solidity with
 * respect to language function signatures encoding and
 * storage layout
 *
 * Below is Java <=> Solidity types mapping:
 *
 *  Input arguments Java -> Solidity mapping is the following:
 *    Number, BigInteger, String (hex) -> any integer type
 *    byte[], String (hex) -> bytesN, byte[]
 *    String -> string
 *    Java array of the above types -> Solidity dynamic array of the corresponding type
 *
 *  Output arguments Solidity -> Java mapping:
 *    any integer type -> BigInteger
 *    string -> String
 *    bytesN, byte[] -> byte[]
 *    Solidity dynamic array -> Java array
 *
 * Created by Jay Nicolas on 23.03.2018.
 */
public interface SolidityContract extends Contract {

    /**
     * Submits the transaction which invokes the specified contract function
     * with corresponding arguments
     *
     * TODO: either return pending transaction execution result
     * or return Future which is available upon block including trnasaction
     * or combine both approaches
     */
    SolidityCallResult callFunction(String functionName, Object ... args);

    /**
     * Submits the transaction which invokes the specified contract function
     * with corresponding arguments and sends the specified value to the contract
     */
    SolidityCallResult callFunction(long value, String functionName, Object ... args);

    /**
     * Call the function without submitting a transaction and without
     * modifying the contract state.
     * Synchronously returns function execution result
     * (see output argument mapping in class doc)
     */
    Object[] callConstFunction(String functionName, Object ... args);

    /**
     * Call the function without submitting a transaction and without
     * modifying the contract state. The function is executed with the
     * contract state actual after including the specified block.
     *
     * Synchronously returns function execution result
     * (see output argument mapping in class doc)
     */
    Object[] callConstFunction(Block callBlock, String functionName, Object... args);

    /**
     * Gets the contract function. This object can be passed as a call argument for another
     * function with a function type parameter
     */
    SolidityFunction getFunction(String name);

    /**
     * Returns the Solidity JSON ABI (Application Binary Interface)
     */
    String getABI();
}
