package com.game.frame.security.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 * 实现行级数据权限、字段级权限、动态SQL注入
 * @author lx
 * @date 2025/06/08
 */
@Service
public class DataPermissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataPermissionService.class);
    
    @Autowired
    private RbacService rbacService;
    
    /**
     * 检查用户对特定数据行的访问权限
     */
    public boolean checkRowPermission(Long userId, String resource, Object dataId, String action) {
        try {
            // 获取用户权限
            Set<String> userPermissions = rbacService.getUserPermissions(userId);
            
            // 检查基本资源权限
            String requiredPermission = resource + "." + action;
            if (!userPermissions.contains(requiredPermission)) {
                logger.debug("User {} lacks basic permission: {}", userId, requiredPermission);
                return false;
            }
            
            // 检查行级权限
            DataPermissionRule rule = getDataPermissionRule(userId, resource);
            if (rule == null) {
                // 没有特殊规则，允许访问
                return true;
            }
            
            return evaluateDataPermissionRule(rule, dataId, action);
            
        } catch (Exception e) {
            logger.error("Failed to check row permission for user: {} resource: {} dataId: {}", userId, resource, dataId, e);
            return false;
        }
    }
    
    /**
     * 检查字段级权限
     */
    public boolean checkFieldPermission(Long userId, String resource, String fieldName, String action) {
        try {
            // 获取用户权限
            Set<String> userPermissions = rbacService.getUserPermissions(userId);
            
            // 检查字段级权限
            String fieldPermission = resource + "." + fieldName + "." + action;
            if (userPermissions.contains(fieldPermission)) {
                return true;
            }
            
            // 检查通用字段权限
            String genericFieldPermission = resource + ".field." + action;
            return userPermissions.contains(genericFieldPermission);
            
        } catch (Exception e) {
            logger.error("Failed to check field permission for user: {} resource: {} field: {}", userId, resource, fieldName, e);
            return false;
        }
    }
    
    /**
     * 过滤用户可访问的数据
     */
    public <T> List<T> filterAccessibleData(Long userId, String resource, List<T> dataList, DataExtractor<T> extractor) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                return dataList;
            }
            
            // 获取数据权限规则
            DataPermissionRule rule = getDataPermissionRule(userId, resource);
            if (rule == null) {
                // 没有特殊规则，返回所有数据
                return dataList;
            }
            
            return dataList.stream()
                    .filter(data -> {
                        Object dataId = extractor.extractId(data);
                        return evaluateDataPermissionRule(rule, dataId, "read");
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Failed to filter accessible data for user: {} resource: {}", userId, resource, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 过滤用户可访问的字段
     */
    public Map<String, Object> filterAccessibleFields(Long userId, String resource, Map<String, Object> data) {
        try {
            if (data == null || data.isEmpty()) {
                return data;
            }
            
            Map<String, Object> filteredData = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                if (checkFieldPermission(userId, resource, fieldName, "read")) {
                    filteredData.put(fieldName, entry.getValue());
                } else {
                    // 敏感字段脱敏
                    filteredData.put(fieldName, maskSensitiveField(fieldName, entry.getValue()));
                }
            }
            
            return filteredData;
            
        } catch (Exception e) {
            logger.error("Failed to filter accessible fields for user: {} resource: {}", userId, resource, e);
            return Collections.emptyMap(); // 发生错误时返回空映射以确保安全
        }
    }
    
    /**
     * 生成数据权限SQL条件
     */
    public String generateDataPermissionSql(Long userId, String resource, String tableAlias) {
        try {
            DataPermissionRule rule = getDataPermissionRule(userId, resource);
            if (rule == null) {
                return ""; // 没有特殊规则，不添加条件
            }
            
            return buildSqlCondition(rule, tableAlias);
            
        } catch (Exception e) {
            logger.error("Failed to generate data permission SQL for user: {} resource: {}", userId, resource, e);
            return "1=0"; // 发生错误时拒绝所有访问
        }
    }
    
    /**
     * 获取用户的数据权限规则
     */
    private DataPermissionRule getDataPermissionRule(Long userId, String resource) {
        // TODO: 从数据库或缓存中获取用户的数据权限规则
        // 这里提供一个简单的模拟实现
        
        Set<String> userRoles = rbacService.getUserRoles(userId);
        
        // 超级管理员拥有所有权限
        if (userRoles.contains("SUPER_ADMIN")) {
            return null; // 无限制
        }
        
        // 管理员可以访问所有数据
        if (userRoles.contains("ADMIN")) {
            return null; // 无限制
        }
        
        // 普通用户只能访问自己的数据
        if (userRoles.contains("USER")) {
            DataPermissionRule rule = new DataPermissionRule();
            rule.setConditionType("OWNER");
            rule.setConditionValue(userId.toString());
            rule.setResourceType(resource);
            return rule;
        }
        
        // 访客无法访问数据
        return new DataPermissionRule("DENY", "all", resource);
    }
    
    /**
     * 评估数据权限规则
     */
    private boolean evaluateDataPermissionRule(DataPermissionRule rule, Object dataId, String action) {
        if (rule == null) {
            return true;
        }
        
        switch (rule.getConditionType()) {
            case "DENY":
                return false;
            case "OWNER":
                // 检查数据是否属于用户
                return checkDataOwnership(rule.getConditionValue(), dataId);
            case "DEPARTMENT":
                // 检查数据是否属于用户部门
                return checkDepartmentAccess(rule.getConditionValue(), dataId);
            case "ROLE_BASED":
                // 基于角色的数据访问
                return checkRoleBasedAccess(rule.getConditionValue(), dataId, action);
            default:
                logger.warn("Unknown data permission condition type: {}", rule.getConditionType());
                return false;
        }
    }
    
    /**
     * 检查数据所有权
     */
    private boolean checkDataOwnership(String ownerUserId, Object dataId) {
        // TODO: 实际实现应该查询数据库
        // 这里提供一个简单的模拟实现
        return true;
    }
    
    /**
     * 检查部门访问权限
     */
    private boolean checkDepartmentAccess(String departmentId, Object dataId) {
        // TODO: 实际实现应该查询数据库
        return true;
    }
    
    /**
     * 检查基于角色的访问权限
     */
    private boolean checkRoleBasedAccess(String roleCondition, Object dataId, String action) {
        // TODO: 实际实现应该根据角色条件检查权限
        return true;
    }
    
    /**
     * 构建SQL条件
     */
    private String buildSqlCondition(DataPermissionRule rule, String tableAlias) {
        String alias = tableAlias.isEmpty() ? "" : tableAlias + ".";
        
        switch (rule.getConditionType()) {
            case "DENY":
                return "1=0"; // 拒绝所有
            case "OWNER":
                return alias + "user_id = " + rule.getConditionValue();
            case "DEPARTMENT":
                return alias + "department_id = '" + rule.getConditionValue() + "'";
            case "ROLE_BASED":
                return buildRoleBasedSqlCondition(rule.getConditionValue(), alias);
            default:
                return "";
        }
    }
    
    /**
     * 构建基于角色的SQL条件
     */
    private String buildRoleBasedSqlCondition(String roleCondition, String alias) {
        // TODO: 根据角色条件构建复杂的SQL条件
        return "";
    }
    
    /**
     * 脱敏敏感字段
     */
    private Object maskSensitiveField(String fieldName, Object value) {
        if (value == null) {
            return null;
        }
        
        String stringValue = value.toString();
        
        // 根据字段名进行不同的脱敏处理
        switch (fieldName.toLowerCase()) {
            case "phone":
            case "mobile":
                return maskPhone(stringValue);
            case "email":
                return maskEmail(stringValue);
            case "idcard":
            case "identity":
                return maskIdCard(stringValue);
            case "bankcard":
            case "card":
                return maskBankCard(stringValue);
            default:
                // 通用脱敏：显示前2位和后2位
                return maskGeneric(stringValue);
        }
    }
    
    private String maskPhone(String phone) {
        if (phone.length() <= 6) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) return "***@***";
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }
    
    private String maskIdCard(String idCard) {
        if (idCard.length() <= 6) return "***";
        return idCard.substring(0, 6) + "***" + idCard.substring(idCard.length() - 4);
    }
    
    private String maskBankCard(String bankCard) {
        if (bankCard.length() <= 8) return "***";
        return bankCard.substring(0, 4) + "***" + bankCard.substring(bankCard.length() - 4);
    }
    
    private String maskGeneric(String value) {
        if (value.length() <= 4) return "***";
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }
    
    /**
     * 数据提取器接口
     */
    @FunctionalInterface
    public interface DataExtractor<T> {
        Object extractId(T data);
    }
    
    /**
     * 数据权限规则
     */
    public static class DataPermissionRule {
        private String conditionType;
        private String conditionValue;
        private String resourceType;
        
        public DataPermissionRule() {}
        
        public DataPermissionRule(String conditionType, String conditionValue, String resourceType) {
            this.conditionType = conditionType;
            this.conditionValue = conditionValue;
            this.resourceType = resourceType;
        }
        
        // Getters and setters
        public String getConditionType() { return conditionType; }
        public void setConditionType(String conditionType) { this.conditionType = conditionType; }
        public String getConditionValue() { return conditionValue; }
        public void setConditionValue(String conditionValue) { this.conditionValue = conditionValue; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    }
}