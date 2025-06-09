package com.game.common.game;

/**
 * 物品实体
 *
 * @author lx
 * @date 2025/06/08
 */
public class Item {
    
    private long itemUid;      // 唯一ID
    private int itemId;        // 配置ID
    private int count;         // 数量
    private boolean bound;     // 绑定状态
    private long expireTime;   // 过期时间
    
    public Item() {}
    
    public Item(long itemUid, int itemId, int count) {
        this.itemUid = itemUid;
        this.itemId = itemId;
        this.count = count;
        this.bound = false;
        this.expireTime = 0;
    }
    
    public long getItemUid() {
        return itemUid;
    }
    
    public void setItemUid(long itemUid) {
        this.itemUid = itemUid;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public boolean isBound() {
        return bound;
    }
    
    public void setBound(boolean bound) {
        this.bound = bound;
    }
    
    public long getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
    
    /**
     * 检查物品是否过期
     */
    public boolean isExpired() {
        return expireTime > 0 && System.currentTimeMillis() > expireTime;
    }
}