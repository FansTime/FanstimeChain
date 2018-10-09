package com.fanstime.fti;

import com.fanstime.fti.vm.DataWord;
import com.fanstime.fti.vm.program.ProgramObject;

public class SmartContract {

    ProgramObject program;

//
//    void sentTickets(byte address, byte num){
//        program.storageLoad(new byte[]{30}).intValue();
//        byte[] a  = new byte[]{(byte)(program.storageLoad(new byte[]{num}).intValue()+1)};
//        if (program.storageLoad(new byte[]{address}) != null) {
//            int beforeNum = program.storageLoad(new byte[]{address}).intValue();
//            program.storageSave(new byte[]{address}, new byte[]{num});
//        } else {
//            program.storageSave(new byte[]{address}, new byte[]{address});
//        }
//    }

    void sendTickets(DataWord d1, DataWord d2) {
        //int sum = d1.intValue() + d2.intValue();
        program.storageSave(d1, d2);
        program.print("Sample SSAVE/SLOAD = " + program.toJsonHex(program.storageLoad(new byte[]{10})));
    }
//    void sentTickets(byte a,byte num){
//                if (true) {        program.storageSave(new byte[]{a}, new byte[]{num});
//                         }
//        }

//    void sentTickets(byte a,byte num){
//                if (true) {
//                       program.storageSave(new byte[]{a}, new byte[]{num});
//                }
//
//                        program.storageLoad(new byte[]{a});
//                }



}
