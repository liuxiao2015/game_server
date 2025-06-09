package com.game.frame.security.auth;

import com.game.frame.security.config.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token提供者
 * @author lx
 * @date 2025/06/08
 */
@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Autowired
    private SecurityProperties securityProperties;

    /**
     * Token生成（包含用户信息、权限、过期时间）
     */
    public String generateToken(AuthUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("sessionId", user.getSessionId());
        if (user.getPermissions() != null) {
            claims.put("permissions", user.getPermissions());
        }
        if (user.getRoles() != null) {
            claims.put("roles", user.getRoles());
        }
        claims.put("loginTime", user.getLoginTime());
        claims.put("loginIp", user.getLoginIp());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + securityProperties.getJwt().getExpiration() * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Token验证
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Token解析
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Token刷新
     */
    public String refreshToken(String oldToken) {
        try {
            Claims claims = parseToken(oldToken);
            if (claims == null) {
                return null;
            }

            // Check if token is in refresh window
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remainingTime = expiration.getTime() - now.getTime();
            
            if (remainingTime < securityProperties.getJwt().getRefreshWindow() * 1000) {
                // Create new token with same claims but new expiration
                Date newExpiration = new Date(now.getTime() + securityProperties.getJwt().getExpiration() * 1000);
                
                return Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(now)
                        .setExpiration(newExpiration)
                        .signWith(getSignKey(), SignatureAlgorithm.HS256)
                        .compact();
            }
            
            return null; // Not in refresh window
        } catch (Exception e) {
            logger.warn("Failed to refresh JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取用户信息
     */
    public AuthUser extractUserFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }

        AuthUser user = new AuthUser();
        user.setUserId(claims.get("userId", Long.class));
        user.setUsername(claims.getSubject());
        user.setSessionId(claims.get("sessionId", String.class));
        user.setLoginTime(claims.get("loginTime", Long.class));
        user.setLoginIp(claims.get("loginIp", String.class));
        
        // Handle permissions and roles arrays
        Object perms = claims.get("permissions");
        if (perms instanceof String[]) {
            user.setPermissions((String[]) perms);
        }
        
        Object roles = claims.get("roles");
        if (roles instanceof String[]) {
            user.setRoles((String[]) roles);
        }

        return user;
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(securityProperties.getJwt().getSecret().getBytes());
    }
}