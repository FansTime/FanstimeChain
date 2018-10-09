package com.fanstime.fti.web.service;

public interface ClientMessageService {
    void sendToTopic(String topic, Object dto);
}
