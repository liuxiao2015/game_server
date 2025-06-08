package com.game.frame.netty.protocol;

/**
 * Message wrapper containing message ID and payload data
 *
 * @author lx
 * @date 2024-01-01
 */
public class MessageWrapper {
    
    private final int messageId;
    private final byte[] payload;
    
    /**
     * Creates a new MessageWrapper
     * 
     * @param messageId message ID
     * @param payload message payload
     */
    public MessageWrapper(int messageId, byte[] payload) {
        this.messageId = messageId;
        this.payload = payload;
    }
    
    /**
     * Gets the message ID
     * 
     * @return message ID
     */
    public int getMessageId() {
        return messageId;
    }
    
    /**
     * Gets the payload
     * 
     * @return payload bytes
     */
    public byte[] getPayload() {
        return payload;
    }
    
    @Override
    public String toString() {
        return "MessageWrapper{" +
                "messageId=" + messageId +
                ", payloadLength=" + (payload != null ? payload.length : 0) +
                '}';
    }
}