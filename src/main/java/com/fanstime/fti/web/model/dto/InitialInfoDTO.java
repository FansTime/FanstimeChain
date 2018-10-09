package com.fanstime.fti.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Created by Jon Williams on 18.03.18.
 */
@Value
@AllArgsConstructor
public class InitialInfoDTO {

    private final String ftiVersion;

    private final String ftiBuildInfo;

    private final String appVersion;

    private final String networkName;

    /**
     * Link to block explore site for contracts import
     */
    private final String explorerUrl;

    private final String genesisHash;

    private final Long serverStartTime;

    private final String nodeId;

    private final Integer rpcPort;

    private final boolean privateNetwork;

    private final String portCheckerUrl;

    private final String publicIp;

    private final boolean featureContracts;

    private final boolean featureRpc;
}
