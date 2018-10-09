package com.fanstime.fti.config;

import java.util.Objects;

/**
 * Chain properties are here
 * For umJ properties check {@link com.fanstime.fti.config.SystemProperties}
 */
public class ChainProperties {

    public static ChainProperties DEFAULT = new ChainProperties(SystemProperties.getDefault());

    private SystemProperties config;

    public ChainProperties(SystemProperties config) {
        this.config = config;
    }

    public boolean isWebEnabled() {
        return false;
    }

    public boolean isRpcEnabled() {
        return config.getConfig().getBoolean("modules.rpc.enabled");
    }

    public Integer rpcPort() {
        if (config.getConfig().hasPath("modules.rpc.port") && isRpcEnabled()) {
            return config.getConfig().getInt("modules.rpc.port");
        } else if (isRpcEnabled()) {
            return getServerPort();
        }

        return null;
    }

    public Integer webPort() {
        if (config.getConfig().hasPath("modules.web.port") && isWebEnabled()) {
            return config.getConfig().getInt("modules.web.port");
        } else if (isWebEnabled()) {
            return getServerPort();
        }

        return null;
    }

    private Integer getServerPort() {
        String serverPort = System.getProperty("server.port");
        if (serverPort != null) {
            return Integer.valueOf(serverPort);
        } else {
            return 8080;
        }
    }

    /**
     * Whether web and rpc runs on one port
     */
    public boolean isWebRpcOnePort() {
        return Objects.equals(webPort(), rpcPort());
    }

    public boolean isContractStorageEnabled() {
        return config.getConfig().getBoolean("modules.contracts.enabled");
    }
}
