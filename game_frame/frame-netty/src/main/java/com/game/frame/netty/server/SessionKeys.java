package com.game.frame.netty.server;

import io.netty.util.AttributeKey;
import com.game.frame.netty.session.Session;

/**
 * Session attribute keys for storing session data in Netty channels
 *
 * @author lx
 * @date 2024-01-01
 */
public final class SessionKeys {
    
    /**
     * Session attribute key
     */
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");
    
    private SessionKeys() {
        // Utility class, no instantiation
    }
}