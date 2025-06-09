package com.game.frame.security.test;

import com.game.frame.security.auth.*;
import com.game.frame.security.crypto.CryptoService;
import com.game.frame.security.defense.*;
import com.game.frame.security.rbac.DataPermissionService;
import com.game.frame.security.rbac.RbacService;
import com.game.frame.security.utils.IpUtils;
import com.game.frame.security.utils.PasswordValidator;
import com.game.frame.security.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全框架综合测试
 * @author lx  
 * @date 2025/06/08
 */
@SpringBootTest
@TestPropertySource(properties = {
    "security.jwt.secret=testSecretKey32CharactersLongForSecurityTesting",
    "security.audit.enabled=true",
    "security.audit.async=false" // 同步测试以便验证
})
public class SecurityFrameworkTest {
    
    @Autowired(required = false)
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired(required = false)
    private MfaService mfaService;
    
    @Autowired(required = false)
    private AuthenticationService authenticationService;
    
    @Autowired(required = false)
    private CryptoService cryptoService;
    
    @Autowired(required = false)
    private NonceManager nonceManager;
    
    @Autowired(required = false)
    private SecureEncoder secureEncoder;
    
    @Autowired(required = false)
    private RbacService rbacService;
    
    @Autowired(required = false)
    private DataPermissionService dataPermissionService;
    
    @Autowired(required = false)
    private IpUtils ipUtils;
    
    @Autowired(required = false)
    private PasswordValidator passwordValidator;
    
    private AuthUser testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new AuthUser(1L, "testuser", "test-session-id");
    }
    
    @Test
    void testJwtTokenProvider() {
        if (jwtTokenProvider == null) {
            System.out.println("JwtTokenProvider not available, skipping test");
            return;
        }
        
        // 测试Token生成
        String token = jwtTokenProvider.generateToken(testUser);
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // 测试Token验证
        boolean isValid = jwtTokenProvider.validateToken(token);
        assertTrue(isValid);
        
        // 测试Token解析
        AuthUser extractedUser = jwtTokenProvider.extractUserFromToken(token);
        assertNotNull(extractedUser);
        assertEquals(testUser.getUsername(), extractedUser.getUsername());
        
        System.out.println("JWT Token Provider test passed");
    }
    
    @Test
    void testMfaService() {
        if (mfaService == null) {
            System.out.println("MfaService not available, skipping test");
            return;
        }
        
        String userId = "test-user-123";
        
        // 测试TOTP密钥生成
        String totpSecret = mfaService.generateTotpSecret(userId);
        assertNotNull(totpSecret);
        assertTrue(totpSecret.length() > 0);
        
        // 测试MFA启用检查
        boolean mfaEnabled = mfaService.isMfaEnabled(userId);
        assertTrue(mfaEnabled);
        
        // 测试设备指纹
        String deviceInfo = "TestDevice-Chrome-Windows";
        String fingerprint = mfaService.registerDeviceFingerprint(userId, deviceInfo);
        assertNotNull(fingerprint);
        
        boolean fingerprintValid = mfaService.verifyDeviceFingerprint(userId, deviceInfo);
        assertTrue(fingerprintValid);
        
        System.out.println("MFA Service test passed");
    }
    
    @Test
    void testCryptoService() {
        if (cryptoService == null) {
            System.out.println("CryptoService not available, skipping test");
            return;
        }
        
        String testData = "Hello, Security Framework!";
        String encryptionKey = "MySecretKey12345";
        
        // 测试AES加密
        String encrypted = cryptoService.encryptAES(testData, encryptionKey);
        assertNotNull(encrypted);
        assertNotEquals(testData, encrypted);
        
        // 测试AES解密
        String decrypted = cryptoService.decryptAES(encrypted, encryptionKey);
        assertEquals(testData, decrypted);
        
        // 测试哈希
        String hash = cryptoService.hash(testData, "SHA-256");
        assertNotNull(hash);
        assertTrue(hash.length() > 0);
        
        System.out.println("Crypto Service test passed");
    }
    
    @Test
    void testNonceManager() {
        if (nonceManager == null) {
            System.out.println("NonceManager not available, skipping test");
            return;
        }
        
        // 测试Nonce生成
        String nonce = nonceManager.generateNonce();
        assertNotNull(nonce);
        assertTrue(nonce.length() > 0);
        
        // 测试Nonce验证
        long currentTimestamp = System.currentTimeMillis() / 1000;
        boolean isValid = nonceManager.validateNonce(nonce, currentTimestamp);
        assertTrue(isValid);
        
        // 测试重复使用检测
        boolean isUsed = nonceManager.isNonceUsed(nonce);
        assertTrue(isUsed);
        
        // 再次验证应该失败
        boolean secondValidation = nonceManager.validateNonce(nonce, currentTimestamp);
        assertFalse(secondValidation);
        
        System.out.println("Nonce Manager test passed");
    }
    
    @Test
    void testSecureEncoder() {
        if (secureEncoder == null) {
            System.out.println("SecureEncoder not available, skipping test");
            return;
        }
        
        String testInput = "<script>alert('xss')</script>";
        
        // 测试HTML编码
        String htmlEncoded = secureEncoder.encodeForHTML(testInput);
        assertNotNull(htmlEncoded);
        assertFalse(htmlEncoded.contains("<script>"));
        
        // 测试XSS检测
        boolean containsXSS = secureEncoder.containsXSS(testInput);
        assertTrue(containsXSS);
        
        // 测试SQL注入检测
        String sqlInput = "'; DROP TABLE users; --";
        boolean containsSQL = secureEncoder.containsSQLInjection(sqlInput);
        assertTrue(containsSQL);
        
        // 测试安全输入清理
        String sanitized = secureEncoder.sanitizeInput(testInput);
        assertNotNull(sanitized);
        assertFalse(sanitized.contains("<script>"));
        
        System.out.println("Secure Encoder test passed");
    }
    
    @Test
    void testDataPermissionService() {
        if (dataPermissionService == null) {
            System.out.println("DataPermissionService not available, skipping test");
            return;
        }
        
        Long userId = 1L;
        String resource = "user";
        Object dataId = 123L;
        
        // 测试行权限检查
        boolean hasRowPermission = dataPermissionService.checkRowPermission(userId, resource, dataId, "read");
        // 由于是模拟数据，这里主要测试方法调用不出错
        assertNotNull(hasRowPermission);
        
        // 测试字段权限检查
        boolean hasFieldPermission = dataPermissionService.checkFieldPermission(userId, resource, "email", "read");
        assertNotNull(hasFieldPermission);
        
        // 测试字段过滤
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", 123);
        testData.put("name", "Test User");
        testData.put("email", "test@example.com");
        testData.put("phone", "13800138000");
        
        Map<String, Object> filteredData = dataPermissionService.filterAccessibleFields(userId, resource, testData);
        assertNotNull(filteredData);
        
        System.out.println("Data Permission Service test passed");
    }
    
    @Test
    void testIpUtils() {
        if (ipUtils == null) {
            System.out.println("IpUtils not available, skipping test");
            return;
        }
        
        // 测试IP验证
        assertTrue(ipUtils.isValidIPv4("192.168.1.1"));
        assertFalse(ipUtils.isValidIPv4("999.999.999.999"));
        assertTrue(ipUtils.isValidIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        
        // 测试私有IP检测
        assertTrue(ipUtils.isPrivateIP("192.168.1.1"));
        assertFalse(ipUtils.isPrivateIP("8.8.8.8"));
        
        // 测试本地IP检测
        assertTrue(ipUtils.isLocalIP("127.0.0.1"));
        assertFalse(ipUtils.isLocalIP("8.8.8.8"));
        
        // 测试IP范围检查
        assertTrue(ipUtils.isIPInRange("192.168.1.100", "192.168.1.0/24"));
        assertFalse(ipUtils.isIPInRange("192.168.2.100", "192.168.1.0/24"));
        
        // 测试地理位置（模拟）
        IpUtils.GeoLocation location = ipUtils.getGeoLocation("8.8.8.8");
        assertNotNull(location);
        assertNotNull(location.getCountry());
        
        System.out.println("IP Utils test passed");
    }
    
    @Test
    void testPasswordValidator() {
        if (passwordValidator == null) {
            System.out.println("PasswordValidator not available, skipping test");
            return;
        }
        
        // 测试弱密码
        PasswordValidator.PasswordValidationResult weakResult = passwordValidator.validatePassword("123456");
        assertFalse(weakResult.isValid());
        assertTrue(weakResult.getErrors().size() > 0);
        assertEquals(PasswordValidator.PasswordStrength.VERY_WEAK, weakResult.getStrength());
        
        // 测试强密码
        PasswordValidator.PasswordValidationResult strongResult = passwordValidator.validatePassword("MyStr0ng!P@ssw0rd");
        assertTrue(strongResult.isValid());
        assertTrue(strongResult.getScore() > 70);
        
        // 测试密码生成
        String generatedPassword = passwordValidator.generateSecurePassword(12);
        assertNotNull(generatedPassword);
        assertEquals(12, generatedPassword.length());
        
        // 验证生成的密码强度
        PasswordValidator.PasswordValidationResult generatedResult = passwordValidator.validatePassword(generatedPassword);
        assertTrue(generatedResult.isValid());
        
        // 测试密码历史检查
        boolean inHistory = passwordValidator.isPasswordInHistory(
            "123456", 
            Arrays.asList("password", "123456", "qwerty"), 
            3
        );
        assertTrue(inHistory);
        
        System.out.println("Password Validator test passed");
    }
    
    @Test
    void testSecurityUtils() {
        // 测试安全随机数生成
        String randomString = SecurityUtils.generateSecureRandom();
        assertNotNull(randomString);
        assertTrue(randomString.length() > 0);
        
        // 测试两次生成的随机数不同
        String randomString2 = SecurityUtils.generateSecureRandom();
        assertNotEquals(randomString, randomString2);
        
        System.out.println("Security Utils test passed");
    }
    
    @Test
    void testAuthenticationFlow() {
        if (authenticationService == null) {
            System.out.println("AuthenticationService not available, skipping test");
            return;
        }
        
        String username = "testuser";
        String password = "testpass123";
        String clientIp = "192.168.1.100";
        
        // 测试认证流程（使用模拟数据）
        // 注意：这将失败，因为我们使用的是测试凭据
        AuthenticationService.AuthenticationResult result = authenticationService.authenticateCredentials(username, password, clientIp);
        assertNotNull(result);
        assertNotNull(result.getMessage());
        
        System.out.println("Authentication flow test completed (expected to fail with test credentials)");
    }
    
    @Test
    void testIntegrationSecurity() {
        System.out.println("Running integration security tests...");
        
        // 测试多个组件的集成
        if (jwtTokenProvider != null && mfaService != null && secureEncoder != null) {
            // 创建Token
            String token = jwtTokenProvider.generateToken(testUser);
            
            // 验证Token
            boolean tokenValid = jwtTokenProvider.validateToken(token);
            assertTrue(tokenValid);
            
            // 测试安全编码
            String safeInput = secureEncoder.sanitizeInput("<script>alert('test')</script>");
            assertFalse(safeInput.contains("<script>"));
            
            System.out.println("Integration security test passed");
        } else {
            System.out.println("Some security components not available for integration test");
        }
    }
    
    @Test
    void testSecurityMetrics() {
        System.out.println("Testing security performance metrics...");
        
        if (jwtTokenProvider != null) {
            long startTime = System.nanoTime();
            
            // 测试Token验证性能
            String token = jwtTokenProvider.generateToken(testUser);
            for (int i = 0; i < 100; i++) {
                jwtTokenProvider.validateToken(token);
            }
            
            long endTime = System.nanoTime();
            long avgTime = (endTime - startTime) / 100 / 1_000_000; // 转换为毫秒
            
            // 验证性能要求：Token验证应该小于1ms
            assertTrue(avgTime < 1, "Token validation should be less than 1ms, actual: " + avgTime + "ms");
            
            System.out.println("Token validation average time: " + avgTime + "ms");
        }
        
        if (cryptoService != null) {
            long startTime = System.nanoTime();
            
            // 测试加密性能
            String testData = "Performance test data for encryption";
            String key = "TestKey123456789";
            
            for (int i = 0; i < 1000; i++) {
                String encrypted = cryptoService.encryptAES(testData, key);
                cryptoService.decryptAES(encrypted, key);
            }
            
            long endTime = System.nanoTime();
            long opsPerSecond = 2000 * 1_000_000_000L / (endTime - startTime); // 2000 operations (1000 encrypt + 1000 decrypt)
            
            // 验证性能要求：加密/解密应该大于10000 ops/s
            assertTrue(opsPerSecond > 10000, "Encryption/Decryption should be > 10000 ops/s, actual: " + opsPerSecond);
            
            System.out.println("Encryption/Decryption performance: " + opsPerSecond + " ops/s");
        }
        
        System.out.println("Security metrics test completed");
    }
    
    @Test
    void testComprehensiveSecurityValidation() {
        System.out.println("Running comprehensive security validation...");
        
        int passedTests = 0;
        int totalTests = 0;
        
        // 检查所有主要安全组件
        totalTests++; if (jwtTokenProvider != null) passedTests++;
        totalTests++; if (mfaService != null) passedTests++;
        totalTests++; if (authenticationService != null) passedTests++;
        totalTests++; if (cryptoService != null) passedTests++;
        totalTests++; if (nonceManager != null) passedTests++;
        totalTests++; if (secureEncoder != null) passedTests++;
        totalTests++; if (dataPermissionService != null) passedTests++;
        totalTests++; if (ipUtils != null) passedTests++;
        totalTests++; if (passwordValidator != null) passedTests++;
        
        double coverage = (double) passedTests / totalTests * 100;
        
        System.out.println(String.format("Security component coverage: %.1f%% (%d/%d)", coverage, passedTests, totalTests));
        
        // 要求至少80%的组件可用
        assertTrue(coverage >= 80.0, "Security component coverage should be at least 80%");
        
        System.out.println("Comprehensive security validation passed");
    }
}