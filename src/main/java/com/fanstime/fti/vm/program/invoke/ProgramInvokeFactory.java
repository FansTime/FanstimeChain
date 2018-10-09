package com.fanstime.fti.vm.program.invoke;

import com.fanstime.fti.core.Block;
import com.fanstime.fti.core.Repository;
import com.fanstime.fti.core.Transaction;
import com.fanstime.fti.db.BlockStore;
import com.fanstime.fti.vm.DataWord;
import com.fanstime.fti.vm.program.Program;

import java.math.BigInteger;


public interface ProgramInvokeFactory {

    ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                      Repository repository, BlockStore blockStore);

    ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore,
                                            boolean staticCall, boolean byTestingSuite);


}
