package com.game.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 测试数据工厂
 * @author lx
 * @date 2025/06/08
 */
public class TestDataFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataFactory.class);
    private static final Random random = new Random();
    
    /**
     * 创建测试用户数据
     */
    public static List<TestUser> createTestUsers() {
        logger.debug("Creating test users...");
        
        List<TestUser> users = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            TestUser user = TestUser.builder()
                    .id((long) i)
                    .username("test_user_" + i)
                    .email("test" + i + "@game.com")
                    .level(random.nextInt(100) + 1)
                    .coins(random.nextInt(10000))
                    .gems(random.nextInt(1000))
                    .createTime(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .build();
            
            users.add(user);
        }
        
        // Store in cache
        TestFramework.getTestDataCache().put("users", users);
        
        logger.info("Created {} test users", users.size());
        return users;
    }
    
    /**
     * 创建测试物品数据
     */
    public static List<TestItem> createTestItems() {
        logger.debug("Creating test items...");
        
        List<TestItem> items = new ArrayList<>();
        
        // 创建不同类型的物品
        String[] itemTypes = {"WEAPON", "ARMOR", "POTION", "MATERIAL"};
        String[] itemNames = {"Steel Sword", "Iron Shield", "Health Potion", "Magic Crystal"};
        
        for (int i = 1; i <= 20; i++) {
            int typeIndex = (i - 1) % itemTypes.length;
            
            TestItem item = TestItem.builder()
                    .id((long) i)
                    .name(itemNames[typeIndex] + " " + i)
                    .type(itemTypes[typeIndex])
                    .rarity(random.nextInt(5) + 1) // 1-5 stars
                    .price(random.nextInt(1000) + 100)
                    .stackable(itemTypes[typeIndex].equals("POTION") || itemTypes[typeIndex].equals("MATERIAL"))
                    .build();
            
            items.add(item);
        }
        
        // Store in cache
        TestFramework.getTestDataCache().put("items", items);
        
        logger.info("Created {} test items", items.size());
        return items;
    }
    
    /**
     * 创建测试订单数据
     */
    public static List<TestOrder> createTestOrders() {
        logger.debug("Creating test orders...");
        
        List<TestOrder> orders = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            TestOrder order = TestOrder.builder()
                    .id((long) i)
                    .userId((long) (random.nextInt(10) + 1))
                    .amount(random.nextDouble() * 100 + 10) // 10-110
                    .currency("USD")
                    .status(random.nextBoolean() ? "COMPLETED" : "PENDING")
                    .createTime(LocalDateTime.now().minusHours(random.nextInt(24)))
                    .build();
            
            orders.add(order);
        }
        
        // Store in cache
        TestFramework.getTestDataCache().put("orders", orders);
        
        logger.info("Created {} test orders", orders.size());
        return orders;
    }
    
    /**
     * 获取缓存的测试用户
     */
    @SuppressWarnings("unchecked")
    public static List<TestUser> getTestUsers() {
        return (List<TestUser>) TestFramework.getTestDataCache().get("users");
    }
    
    /**
     * 获取缓存的测试物品
     */
    @SuppressWarnings("unchecked")
    public static List<TestItem> getTestItems() {
        return (List<TestItem>) TestFramework.getTestDataCache().get("items");
    }
    
    /**
     * 获取缓存的测试订单
     */
    @SuppressWarnings("unchecked")
    public static List<TestOrder> getTestOrders() {
        return (List<TestOrder>) TestFramework.getTestDataCache().get("orders");
    }
    
    /**
     * 清理测试数据
     */
    public static void cleanup() {
        Map<String, Object> cache = TestFramework.getTestDataCache();
        cache.remove("users");
        cache.remove("items");
        cache.remove("orders");
        logger.info("Test data cleaned up");
    }
    
    // Data model classes
    public static class TestUser {
        private Long id;
        private String username;
        private String email;
        private int level;
        private int coins;
        private int gems;
        private LocalDateTime createTime;
        
        public static TestUser.Builder builder() {
            return new TestUser.Builder();
        }
        
        public static class Builder {
            private TestUser user = new TestUser();
            
            public Builder id(Long id) {
                user.id = id;
                return this;
            }
            
            public Builder username(String username) {
                user.username = username;
                return this;
            }
            
            public Builder email(String email) {
                user.email = email;
                return this;
            }
            
            public Builder level(int level) {
                user.level = level;
                return this;
            }
            
            public Builder coins(int coins) {
                user.coins = coins;
                return this;
            }
            
            public Builder gems(int gems) {
                user.gems = gems;
                return this;
            }
            
            public Builder createTime(LocalDateTime createTime) {
                user.createTime = createTime;
                return this;
            }
            
            public TestUser build() {
                return user;
            }
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public int getLevel() { return level; }
        public int getCoins() { return coins; }
        public int getGems() { return gems; }
        public LocalDateTime getCreateTime() { return createTime; }
    }
    
    public static class TestItem {
        private Long id;
        private String name;
        private String type;
        private int rarity;
        private int price;
        private boolean stackable;
        
        public static TestItem.Builder builder() {
            return new TestItem.Builder();
        }
        
        public static class Builder {
            private TestItem item = new TestItem();
            
            public Builder id(Long id) {
                item.id = id;
                return this;
            }
            
            public Builder name(String name) {
                item.name = name;
                return this;
            }
            
            public Builder type(String type) {
                item.type = type;
                return this;
            }
            
            public Builder rarity(int rarity) {
                item.rarity = rarity;
                return this;
            }
            
            public Builder price(int price) {
                item.price = price;
                return this;
            }
            
            public Builder stackable(boolean stackable) {
                item.stackable = stackable;
                return this;
            }
            
            public TestItem build() {
                return item;
            }
        }
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public int getRarity() { return rarity; }
        public int getPrice() { return price; }
        public boolean isStackable() { return stackable; }
    }
    
    public static class TestOrder {
        private Long id;
        private Long userId;
        private double amount;
        private String currency;
        private String status;
        private LocalDateTime createTime;
        
        public static TestOrder.Builder builder() {
            return new TestOrder.Builder();
        }
        
        public static class Builder {
            private TestOrder order = new TestOrder();
            
            public Builder id(Long id) {
                order.id = id;
                return this;
            }
            
            public Builder userId(Long userId) {
                order.userId = userId;
                return this;
            }
            
            public Builder amount(double amount) {
                order.amount = amount;
                return this;
            }
            
            public Builder currency(String currency) {
                order.currency = currency;
                return this;
            }
            
            public Builder status(String status) {
                order.status = status;
                return this;
            }
            
            public Builder createTime(LocalDateTime createTime) {
                order.createTime = createTime;
                return this;
            }
            
            public TestOrder build() {
                return order;
            }
        }
        
        // Getters
        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public double getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getStatus() { return status; }
        public LocalDateTime getCreateTime() { return createTime; }
    }
}