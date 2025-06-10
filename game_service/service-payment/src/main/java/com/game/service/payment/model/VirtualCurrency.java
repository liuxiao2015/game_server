package com.game.service.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 虚拟货币账户实体类
 * 
 * 功能说明：
 * - 管理游戏内虚拟货币的余额和交易记录
 * - 支持多种货币类型的独立账户管理
 * - 提供货币的冻结和解冻机制，确保交易安全
 * - 实现乐观锁机制，防止并发操作导致的数据不一致
 * 
 * 设计思路：
 * - 采用JPA实体注解，支持数据库持久化和ORM映射
 * - 使用BigDecimal确保货币计算的精度和准确性
 * - 分离余额和冻结金额，支持复杂的交易场景
 * - 记录创建和更新时间，便于审计和数据分析
 * 
 * 货币体系：
 * - DIAMOND：钻石（付费货币），通过充值获得
 * - GOLD：金币（基础货币），可通过游戏或充值获得
 * - SILVER：银币（免费货币），仅通过游戏获得
 * - ENERGY：体力值，限制玩家游戏频率
 * - POINTS：积分，通过活动和成就获得
 * 
 * 使用场景：
 * - 玩家充值和消费的账户管理
 * - 游戏内购买道具和服务的支付
 * - 活动奖励和任务奖励的发放
 * - 交易系统中的资金冻结和释放
 * 
 * 安全特性：
 * - 乐观锁防止并发修改冲突
 * - 余额校验防止负数和非法操作
 * - 冻结机制支持安全的异步交易
 * - 操作日志便于审计和问题追踪
 * 
 * 数据完整性：
 * - 用户ID和货币类型的唯一约束
 * - 余额和冻结金额的非空约束
 * - 精度控制确保货币计算的准确性
 *
 * @author lx
 * @date 2025/01/08
 */
@Entity
@Table(name = "virtual_currency", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "type"}))
public class VirtualCurrency {
    
    /** 虚拟货币账户ID，数据库主键，唯一标识货币账户记录 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 用户ID，关联用户表，标识货币账户的所有者 */
    @Column(nullable = false)
    private Long userId;
    
    /** 货币类型，枚举值，区分不同种类的虚拟货币 */
    @Enumerated(EnumType.STRING)
    private CurrencyType type;
    
    /** 当前余额，使用BigDecimal确保货币计算的精度 */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    /** 冻结金额，用于支持安全的异步交易和预扣款 */
    @Column(name = "frozen_amount", precision = 19, scale = 2)
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    
    /** 版本号，用于乐观锁机制，防止并发操作冲突 */
    @Version
    private Long version;
    
    /** 账户创建时间，记录货币账户的创建时刻 */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /** 最后更新时间，记录账户余额的最后变更时刻 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 虚拟货币类型枚举
     * 
     * 功能说明：
     * - 定义游戏中支持的所有虚拟货币类型
     * - 区分付费货币、免费货币和特殊货币
     * - 便于货币系统的扩展和管理
     * 
     * 货币分类：
     * - 付费货币：通过充值获得，价值较高
     * - 基础货币：可通过游戏或充值获得
     * - 免费货币：仅通过游戏活动获得
     * - 功能货币：限制特定游戏行为
     * - 积分货币：奖励和成就系统使用
     */
    public enum CurrencyType {
        /** 钻石 - 高级付费货币，主要通过充值获得，用于购买稀有道具 */
        DIAMOND,
        
        /** 金币 - 基础货币，可通过游戏获得或充值购买，用于常规交易 */
        GOLD,
        
        /** 银币 - 免费货币，仅通过游戏活动获得，用于基础功能 */
        SILVER,
        
        /** 体力值 - 游戏能量限制，随时间恢复或通过道具补充 */
        ENERGY,
        
        /** 积分 - 活动和成就货币，用于兑换特殊奖励 */
        POINTS
    }

    public VirtualCurrency() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public VirtualCurrency(Long userId, CurrencyType type) {
        this();
        this.userId = userId;
        this.type = type;
    }

    public VirtualCurrency(Long userId, CurrencyType type, BigDecimal balance) {
        this(userId, type);
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    /**
     * Add amount to balance
     */
    public boolean addBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        this.balance = this.balance.add(amount);
        this.updateTime = LocalDateTime.now();
        return true;
    }

    /**
     * Subtract amount from balance
     */
    public boolean subtractBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.balance.compareTo(amount) < 0) {
            return false; // Insufficient balance
        }
        
        this.balance = this.balance.subtract(amount);
        this.updateTime = LocalDateTime.now();
        return true;
    }

    /**
     * Freeze amount (move from balance to frozen)
     */
    public boolean freezeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.balance.compareTo(amount) < 0) {
            return false; // Insufficient balance
        }
        
        this.balance = this.balance.subtract(amount);
        this.frozenAmount = this.frozenAmount.add(amount);
        this.updateTime = LocalDateTime.now();
        return true;
    }

    /**
     * Unfreeze amount (move from frozen to balance)
     */
    public boolean unfreezeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.frozenAmount.compareTo(amount) < 0) {
            return false; // Insufficient frozen amount
        }
        
        this.frozenAmount = this.frozenAmount.subtract(amount);
        this.balance = this.balance.add(amount);
        this.updateTime = LocalDateTime.now();
        return true;
    }

    /**
     * Consume frozen amount (remove from frozen)
     */
    public boolean consumeFrozenAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.frozenAmount.compareTo(amount) < 0) {
            return false; // Insufficient frozen amount
        }
        
        this.frozenAmount = this.frozenAmount.subtract(amount);
        this.updateTime = LocalDateTime.now();
        return true;
    }

    /**
     * Get available balance (balance - frozen)
     */
    public BigDecimal getAvailableBalance() {
        return this.balance.subtract(this.frozenAmount);
    }

    /**
     * Get total balance (balance + frozen)
     */
    public BigDecimal getTotalBalance() {
        return this.balance.add(this.frozenAmount);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CurrencyType getType() {
        return type;
    }

    public void setType(CurrencyType type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.updateTime = LocalDateTime.now();
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount != null ? frozenAmount : BigDecimal.ZERO;
        this.updateTime = LocalDateTime.now();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "VirtualCurrency{" +
                "id=" + id +
                ", userId=" + userId +
                ", type=" + type +
                ", balance=" + balance +
                ", frozenAmount=" + frozenAmount +
                ", version=" + version +
                '}';
    }
}