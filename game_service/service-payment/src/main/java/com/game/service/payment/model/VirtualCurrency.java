package com.game.service.payment.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Virtual currency account entity
 * Manages user's virtual currency balances
 *
 * @author lx
 * @date 2025/01/08
 */
@Entity
@Table(name = "virtual_currency", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "type"}))
public class VirtualCurrency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private CurrencyType type;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "frozen_amount", precision = 19, scale = 2)
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    
    @Version
    private Long version; // For optimistic locking
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * Currency type enumeration
     */
    public enum CurrencyType {
        DIAMOND,    // Premium currency (paid)
        GOLD,       // Basic currency (earned/paid)
        SILVER,     // Free currency (earned)
        ENERGY,     // Game energy/stamina
        POINTS      // Event/achievement points
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