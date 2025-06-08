-- 初始化数据库架构
-- @author lx
-- @date 2025/06/08

-- 创建用户表
CREATE TABLE game_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    nickname VARCHAR(100) COMMENT '昵称',
    level INT NOT NULL DEFAULT 1 COMMENT '等级',
    exp BIGINT NOT NULL DEFAULT 0 COMMENT '经验值',
    vip_level INT NOT NULL DEFAULT 0 COMMENT 'VIP等级',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_last_login (last_login_time),
    INDEX idx_level (level),
    INDEX idx_vip_level (vip_level),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建物品表（分片表示例）
CREATE TABLE game_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_type VARCHAR(50) NOT NULL COMMENT '物品类型',
    item_id VARCHAR(100) NOT NULL COMMENT '物品标识',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    properties JSON COMMENT '物品属性（JSON格式）',
    sharding_key VARCHAR(50) NOT NULL COMMENT '分片键',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_item_type (item_type),
    INDEX idx_sharding_key (sharding_key),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (user_id) REFERENCES game_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品表';

-- 创建订单表（分片表示例）
CREATE TABLE game_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(100) UNIQUE NOT NULL COMMENT '订单号',
    order_type VARCHAR(50) NOT NULL COMMENT '订单类型',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
    sharding_key VARCHAR(50) NOT NULL COMMENT '分片键',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_order_type (order_type),
    INDEX idx_status (status),
    INDEX idx_sharding_key (sharding_key),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (user_id) REFERENCES game_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 初始化示例数据
INSERT INTO game_user (username, nickname, level, exp, vip_level) VALUES
('admin', '管理员', 100, 999999, 10),
('test_user1', '测试用户1', 10, 5000, 1),
('test_user2', '测试用户2', 15, 12000, 2),
('test_user3', '测试用户3', 20, 25000, 3);