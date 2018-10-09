package com.fanstime.fti.vm;

import com.fanstime.fti.vm.program.Program;

/**
 * Created by Jay Nicolas on 15.02.2018.
 */
public interface VMHook {
    void startPlay(Program program);
    void step(Program program, OpCode opcode);
    void stopPlay(Program program);
}
