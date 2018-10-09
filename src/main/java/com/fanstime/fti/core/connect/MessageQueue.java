package com.fanstime.fti.core.connect;

import com.fanstime.fti.listener.FtiListener;
import com.fanstime.fti.core.connect.message.ReasonCode;
import com.fanstime.fti.core.connect.server.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class contains the logic for sending messages in a queue
 * <p>
 * Messages open by send and answered by receive of appropriate message
 * PING by PONG
 * GET_PEERS by PEERS
 * GET_TRANSACTIONS by TRANSACTIONS
 * GET_BLOCK_HASHES by BLOCK_HASHES
 * GET_BLOCKS by BLOCKS
 * <p>
 * The following messages will not be answered:
 * PONG, PEERS, HELLO, STATUS, TRANSACTIONS, BLOCKS
 */
@Component
@Scope("prototype")
public class MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        private AtomicInteger cnt = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            return new Thread(r, "MessageQueueTimer-" + cnt.getAndIncrement());
        }
    });

    private ChannelHandlerContext ctx = null;

    @Autowired
    FtiListener ftiListener;
    private ScheduledFuture<?> timerTask;
    private Channel channel;

    public MessageQueue() {
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public void disconnect(ReasonCode reason) {
    }


    public void close() {
        if (timerTask != null) {
            timerTask.cancel(false);
        }
    }
}
