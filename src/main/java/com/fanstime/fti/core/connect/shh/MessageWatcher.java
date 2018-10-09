package com.fanstime.fti.core.connect.shh;


public abstract class MessageWatcher {
    private String to;
    private String from;
    private Topic[] topics = null;


    public MessageWatcher setTo(String to) {
        this.to = to;
        return this;
    }

    public MessageWatcher setFrom(String from) {
        this.from = from;
        return this;
    }


    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public Topic[] getTopics() {
        return topics == null ? new Topic[0] : topics;
    }



}
