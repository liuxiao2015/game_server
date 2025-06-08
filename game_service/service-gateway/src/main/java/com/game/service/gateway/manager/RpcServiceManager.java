package com.game.service.gateway.manager;

import com.game.common.api.service.IGameService;
import com.game.common.api.service.ISessionService;
import com.game.common.api.service.IUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * RPC service manager for Gateway
 * Manages service references and provides unified access to Logic services
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class RpcServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(RpcServiceManager.class);

    @DubboReference(version = "1.0.0", group = "game", timeout = 3000, check = false)
    private IUserService userService;

    @DubboReference(version = "1.0.0", group = "game", timeout = 3000, check = false)
    private ISessionService sessionService;

    @DubboReference(version = "1.0.0", group = "game", timeout = 3000, check = false)
    private IGameService gameService;

    /**
     * Get user service
     *
     * @return user service
     */
    public IUserService getUserService() {
        return userService;
    }

    /**
     * Get session service
     *
     * @return session service
     */
    public ISessionService getSessionService() {
        return sessionService;
    }

    /**
     * Get game service
     *
     * @return game service
     */
    public IGameService getGameService() {
        return gameService;
    }

    /**
     * Check if all services are available
     *
     * @return true if all services are available
     */
    public boolean checkServicesHealth() {
        try {
            // Simple health check by calling a lightweight method
            boolean userServiceOk = userService != null;
            boolean sessionServiceOk = sessionService != null;
            boolean gameServiceOk = gameService != null;

            boolean allOk = userServiceOk && sessionServiceOk && gameServiceOk;
            
            if (allOk) {
                logger.debug("All RPC services are healthy");
            } else {
                logger.warn("Some RPC services are unhealthy: user={}, session={}, game={}", 
                        userServiceOk, sessionServiceOk, gameServiceOk);
            }
            
            return allOk;
        } catch (Exception e) {
            logger.error("Failed to check services health", e);
            return false;
        }
    }
}