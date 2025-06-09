package com.game.frame.security.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy安全沙箱
 * @author lx
 * @date 2025/06/08
 */
@Component
public class GroovySandbox {
    private static final Logger logger = LoggerFactory.getLogger(GroovySandbox.class);
    
    // Script cache to avoid recompilation
    private final Map<String, Script> scriptCache = new ConcurrentHashMap<>();
    
    // Allowed methods/classes whitelist
    private final Set<String> allowedClasses = new HashSet<>();
    private final Set<String> allowedMethods = new HashSet<>();
    
    public GroovySandbox() {
        initializeWhitelist();
    }

    /**
     * 创建受限的脚本环境
     */
    public GroovyShell createSecureShell() {
        return createSecureShell(new Binding());
    }

    /**
     * 创建受限的脚本环境（带绑定变量）
     */
    public GroovyShell createSecureShell(Binding binding) {
        // Create a secure configuration
        org.codehaus.groovy.control.CompilerConfiguration config = 
            new org.codehaus.groovy.control.CompilerConfiguration();
        
        // Add sandbox transformer
        config.addCompilationCustomizers(new SandboxTransformer());
        
        // Create shell with restricted security
        GroovyShell shell = new GroovyShell(
            this.getClass().getClassLoader(), 
            binding, 
            config
        );
        
        return shell;
    }

    /**
     * 执行脚本
     */
    public Object executeScript(String scriptText, Map<String, Object> bindings) {
        if (scriptText == null || scriptText.trim().isEmpty()) {
            throw new IllegalArgumentException("Script text cannot be null or empty");
        }

        try {
            // Check script for dangerous operations
            validateScript(scriptText);
            
            // Create secure shell
            Binding binding = new Binding();
            if (bindings != null) {
                bindings.forEach(binding::setVariable);
            }
            
            GroovyShell shell = createSecureShell(binding);
            
            // Check cache first
            String cacheKey = scriptText.hashCode() + "";
            Script script = scriptCache.get(cacheKey);
            
            if (script == null) {
                // Compile and cache script
                script = shell.parse(scriptText);
                scriptCache.put(cacheKey, script);
                logger.debug("Compiled and cached script: {}", cacheKey);
            } else {
                // Clone script to avoid shared state
                script = script.getClass().newInstance();
                script.setBinding(binding);
            }
            
            // Execute with sandbox protection
            return executeWithSandbox(script);
            
        } catch (Exception e) {
            logger.error("Script execution failed: {}", e.getMessage());
            throw new ScriptExecutionException("Script execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * 在沙箱中执行脚本
     */
    private Object executeWithSandbox(Script script) throws Exception {
        ScriptSecurityInterceptor interceptor = new ScriptSecurityInterceptor();
        
        try {
            // Register interceptor
            interceptor.register();
            
            // Execute script
            return script.run();
            
        } finally {
            // Always unregister interceptor
            interceptor.unregister();
        }
    }

    /**
     * 验证脚本安全性
     */
    private void validateScript(String scriptText) {
        // Basic security checks
        String lowerScript = scriptText.toLowerCase();
        
        // Check for dangerous operations
        String[] dangerousPatterns = {
            "system.exit", "runtime.getruntime", "processbuilder", 
            "file(", "new file", "fileinputstream", "fileoutputstream",
            "socket", "serversocket", "url(", "urlconnection",
            "class.forname", "classloader", "reflection",
            "thread.sleep", "thread.interrupt", "system.gc"
        };
        
        for (String pattern : dangerousPatterns) {
            if (lowerScript.contains(pattern)) {
                throw new SecurityException("Dangerous operation detected: " + pattern);
            }
        }
        
        // Check script length (prevent DoS)
        if (scriptText.length() > 10000) {
            throw new SecurityException("Script too long");
        }
    }

    /**
     * 初始化白名单
     */
    private void initializeWhitelist() {
        // Allowed classes
        allowedClasses.addAll(Arrays.asList(
            "java.lang.String",
            "java.lang.Integer", 
            "java.lang.Long",
            "java.lang.Double",
            "java.lang.Boolean",
            "java.lang.Math",
            "java.util.List",
            "java.util.Map",
            "java.util.Set",
            "java.util.Date",
            "java.time.LocalDateTime",
            "java.time.LocalDate"
        ));
        
        // Allowed methods
        allowedMethods.addAll(Arrays.asList(
            "toString", "equals", "hashCode", "size", "isEmpty",
            "get", "put", "contains", "add", "remove",
            "substring", "length", "toUpperCase", "toLowerCase",
            "valueOf", "parseInt", "parseDouble"
        ));
    }

    /**
     * 清理脚本缓存
     */
    public void clearCache() {
        scriptCache.clear();
        logger.info("Script cache cleared");
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", scriptCache.size());
        stats.put("allowedClasses", allowedClasses.size());
        stats.put("allowedMethods", allowedMethods.size());
        return stats;
    }

    /**
     * 脚本安全拦截器
     */
    private class ScriptSecurityInterceptor extends GroovyInterceptor {
        
        @Override
        public Object onMethodCall(GroovyInterceptor.Invoker invoker, Object receiver, 
                                   String method, Object... args) throws Throwable {
            
            // Check if method is allowed
            if (!isMethodAllowed(receiver, method)) {
                throw new SecurityException("Method not allowed: " + 
                    (receiver != null ? receiver.getClass().getName() : "null") + "." + method);
            }
            
            return super.onMethodCall(invoker, receiver, method, args);
        }
        
        @Override
        public Object onNewInstance(GroovyInterceptor.Invoker invoker, Class receiver, 
                                    Object... args) throws Throwable {
            
            // Check if class instantiation is allowed
            if (!isClassAllowed(receiver)) {
                throw new SecurityException("Class instantiation not allowed: " + receiver.getName());
            }
            
            return super.onNewInstance(invoker, receiver, args);
        }
        
        private boolean isMethodAllowed(Object receiver, String method) {
            if (receiver == null) {
                return false;
            }
            
            String className = receiver.getClass().getName();
            
            // Check against whitelist
            return allowedClasses.contains(className) && allowedMethods.contains(method);
        }
        
        private boolean isClassAllowed(Class<?> clazz) {
            return allowedClasses.contains(clazz.getName());
        }
    }

    /**
     * 脚本执行异常
     */
    public static class ScriptExecutionException extends RuntimeException {
        public ScriptExecutionException(String message) {
            super(message);
        }
        
        public ScriptExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}