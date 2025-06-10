package com.game.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置表注解
 * 
 * 功能说明：
 * - 标记配置类与JSON配置文件的关联关系
 * - 为配置管理器提供文件名映射信息
 * - 支持配置类的自动加载和解析机制
 * - 实现配置与代码的松耦合设计
 * 
 * 设计思路：
 * - 使用Java注解元编程简化配置管理
 * - 通过运行时反射获取配置文件名
 * - 支持类型安全的配置文件映射
 * - 提供清晰的配置文件命名规范
 * 
 * 注解属性：
 * - value：指定对应的JSON配置文件名
 * - 文件路径相对于classpath的config目录
 * - 支持标准的JSON格式文件
 * 
 * 使用规范：
 * - 必须标注在配置类上（TYPE级别）
 * - 配置文件名应遵循命名规范
 * - 建议使用小写字母和下划线
 * - 文件扩展名统一使用.json
 * 
 * 应用场景：
 * - 游戏配置数据的文件映射
 * - 系统参数的外部化配置
 * - 多环境配置的统一管理
 * - 配置热更新的标识机制
 * 
 * 技术特性：
 * - 运行时保留确保反射可访问
 * - 类型目标限制保证使用正确性
 * - 简洁的API设计便于使用
 * 
 * 示例用法：
 * @ConfigTable("task.json")
 * public class TaskConfig extends TableConfig { ... }
 * 
 * @ConfigTable("monster.json") 
 * public class MonsterConfig extends TableConfig { ... }
 *
 * @author lx
 * @date 2025/06/08
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigTable {
    
    /**
     * 配置文件名属性
     * 
     * 功能说明：
     * - 指定配置类对应的JSON配置文件名
     * - 文件路径相对于classpath的config目录
     * - 用于ConfigManager自动加载配置数据
     * 
     * 命名规范：
     * - 使用小写字母和下划线分隔
     * - 文件扩展名必须为.json
     * - 文件名应具有描述性和唯一性
     * - 建议与配置类名保持相关性
     * 
     * 示例值：
     * - "task.json" - 任务配置文件
     * - "monster.json" - 怪物配置文件
     * - "item.json" - 物品配置文件
     * - "shop.json" - 商店配置文件
     * 
     * @return 配置文件名，包含文件扩展名
     */
    String value();
}