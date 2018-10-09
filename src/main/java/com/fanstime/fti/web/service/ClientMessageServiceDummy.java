package com.fanstime.fti.web.service;

/**
 * Dummy for usage when ClientMessageService is not available
 */
public class ClientMessageServiceDummy implements ClientMessageService {

    @Override
    public void sendToTopic(String topic, Object dto) {
        // nothing to do
    }
}
