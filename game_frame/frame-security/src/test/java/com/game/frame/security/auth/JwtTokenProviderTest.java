package com.game.frame.security.auth;

import com.game.frame.security.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * JWT Token Provider Test
 * @author lx
 * @date 2025/06/08
 */
class JwtTokenProviderTest {

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private SecurityProperties.Jwt jwtProperties;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(securityProperties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getSecret()).thenReturn("testSecretKey32CharactersLongForTesting");
        when(jwtProperties.getExpiration()).thenReturn(3600L); // 1 hour
        when(jwtProperties.getRefreshWindow()).thenReturn(1800L); // 30 minutes
    }

    @Test
    void testGenerateToken() {
        // Given
        AuthUser user = new AuthUser();
        user.setUserId(123L);
        user.setUsername("testuser");
        user.setSessionId("session123");
        user.setLoginIp("192.168.1.1");

        // When
        String token = jwtTokenProvider.generateToken(user);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken() {
        // Given
        AuthUser user = new AuthUser();
        user.setUserId(123L);
        user.setUsername("testuser");
        user.setSessionId("session123");

        String token = jwtTokenProvider.generateToken(user);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testParseToken() {
        // Given
        AuthUser user = new AuthUser();
        user.setUserId(123L);
        user.setUsername("testuser");
        user.setSessionId("session123");

        String token = jwtTokenProvider.generateToken(user);

        // When
        Claims claims = jwtTokenProvider.parseToken(token);

        // Then
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals(123L, claims.get("userId", Long.class));
        assertEquals("session123", claims.get("sessionId", String.class));
    }

    @Test
    void testExtractUserFromToken() {
        // Given
        AuthUser originalUser = new AuthUser();
        originalUser.setUserId(123L);
        originalUser.setUsername("testuser");
        originalUser.setSessionId("session123");
        originalUser.setLoginIp("192.168.1.1");

        String token = jwtTokenProvider.generateToken(originalUser);

        // When
        AuthUser extractedUser = jwtTokenProvider.extractUserFromToken(token);

        // Then
        assertNotNull(extractedUser);
        assertEquals(originalUser.getUserId(), extractedUser.getUserId());
        assertEquals(originalUser.getUsername(), extractedUser.getUsername());
        assertEquals(originalUser.getSessionId(), extractedUser.getSessionId());
        assertEquals(originalUser.getLoginIp(), extractedUser.getLoginIp());
    }

    @Test
    void testIsTokenExpired() {
        // Given
        AuthUser user = new AuthUser();
        user.setUserId(123L);
        user.setUsername("testuser");
        user.setSessionId("session123");

        String token = jwtTokenProvider.generateToken(user);

        // When
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Then
        assertFalse(isExpired); // Token should not be expired immediately
    }
}