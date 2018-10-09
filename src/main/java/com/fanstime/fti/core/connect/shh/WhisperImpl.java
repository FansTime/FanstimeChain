package com.fanstime.fti.core.connect.shh;


import com.fanstime.fti.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WhisperImpl extends Whisper {
    private final static Logger logger = LoggerFactory.getLogger("net.shh");

    private Map<String, ECKey> identities = new HashMap<>();


    @Override
    public void send(String from, String to, byte[] payload, Topic[] topicList, int ttl, int workToProve) {

    }


    public void watch(MessageWatcher f) {

    }

    public void unwatch(MessageWatcher f) {

    }


    public static String toIdentity(ECKey key) {
        return Hex.toHexString(key.getNodeId());
    }

    @Override
    public String addIdentity(ECKey key) {
        String identity = toIdentity(key);
        identities.put(identity, key);
        return identity;
    }

    @Override
    public String newIdentity() {
        return addIdentity(new ECKey());
    }

}
