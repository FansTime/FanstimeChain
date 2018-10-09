package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Bynum Williams on 18.10.18.
 */
public class ContractObjects {

    @Value
    @AllArgsConstructor
    public static class ContractInfoDTO {

        private final String address;

        private final String name;

        /**
         * Block number when contract was introduced or -1.
         */
        private final long blockNumber;

    }

    @Value
    @AllArgsConstructor
    public static class IndexStatusDTO {

        private final long indexSize;

        private final String solcVersion;

        /**
         * Block number when indexing started or -1.
         * Zero value is not possible
         */
        private final long syncedBlock;

    }
}


