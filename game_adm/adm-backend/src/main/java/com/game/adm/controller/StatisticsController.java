package com.game.adm.controller;

import com.game.adm.statistics.StatisticsService;
import com.game.adm.statistics.StatisticsService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 游戏统计数据控制器
 * 
 * 功能说明：
 * - 提供游戏运营数据的RESTful API接口
 * - 支持实时在线统计、用户活跃度分析、收入统计等核心指标
 * - 为管理后台和运营决策提供数据支持
 * - 集成Swagger文档，便于API接口的查看和测试
 * 
 * 设计思路：
 * - 采用RESTful API设计规范，提供标准化的数据接口
 * - 使用Spring Boot Controller注解实现HTTP端点映射
 * - 集成Swagger注解生成自动化API文档
 * - 分离业务逻辑到Service层，保持Controller的简洁性
 * 
 * 核心功能：
 * - 实时在线统计：当前在线人数、峰值人数、趋势分析
 * - 活跃用户分析：DAU/MAU指标、用户活跃度趋势
 * - 收入统计分析：充值收入、ARPU、ARPPU等财务指标
 * - 用户留存分析：1日、7日、30日留存率统计
 * - 付费用户分析：付费转化率、付费用户行为分析
 * 
 * 应用场景：
 * - 游戏运营团队的日常数据监控
 * - 管理层的运营决策数据支持
 * - 第三方BI系统的数据对接
 * - 实时监控大屏的数据展示
 * 
 * 接口设计：
 * - 统一的API路径前缀：/api/adm/statistics
 * - 支持时间范围查询参数
 * - 返回结构化的统计数据对象
 * - 提供详细的接口文档和参数说明
 * 
 * 数据安全：
 * - 管理后台专用接口，需要管理员权限
 * - 敏感财务数据的访问控制
 * - API调用频率限制，防止数据泄露
 * - 操作日志记录，便于审计追踪
 *
 * @author lx
 * @date 2025/06/08
 */
@RestController
@RequestMapping("/api/adm/statistics")
@Tag(name = "统计数据", description = "游戏统计数据相关接口")
public class StatisticsController {
    
    /** 统计服务，提供各类游戏数据的统计和分析功能 */
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取实时在线统计数据
     * 
     * 功能说明：
     * - 提供当前游戏的实时在线人数统计
     * - 包含峰值人数、在线趋势等关键指标
     * - 用于运营团队的实时监控和快速决策
     * 
     * 返回数据：
     * - 当前在线人数：实时的在线玩家数量
     * - 今日峰值人数：当日最高在线人数记录
     * - 在线趋势：近期在线人数的变化趋势
     * - 服务器负载：当前服务器的负载状态
     * 
     * 应用场景：
     * - 运营大屏的实时数据展示
     * - 服务器容量规划的参考数据
     * - 活动效果的即时评估
     * 
     * @return OnlineStatistics 实时在线统计数据对象
     */
    @GetMapping("/online")
    @Operation(summary = "获取实时在线统计", description = "获取当前在线人数和峰值统计")
    public OnlineStatistics getOnlineStatistics() {
        return statisticsService.getRealtimeOnline();
    }
    
    /**
     * 获取活跃用户统计数据
     * 
     * 功能说明：
     * - 分析指定时间范围内的用户活跃情况
     * - 计算DAU（日活跃用户）和MAU（月活跃用户）指标
     * - 提供用户活跃度的趋势分析和对比数据
     * 
     * 参数说明：
     * - startDate：统计开始日期，格式：YYYY-MM-DD
     * - endDate：统计结束日期，格式：YYYY-MM-DD
     * 
     * 返回数据：
     * - DAU数据：每日活跃用户数量和趋势
     * - MAU数据：月活跃用户数量和增长率
     * - 活跃度分析：用户活跃时段分布
     * - 对比数据：与历史同期的对比分析
     * 
     * 应用场景：
     * - 产品健康度评估
     * - 市场推广效果分析
     * - 用户生命周期管理
     * 
     * @param startDate 统计开始日期
     * @param endDate 统计结束日期
     * @return ActiveUserStatistics 活跃用户统计数据对象
     */
    @GetMapping("/active-users")
    @Operation(summary = "获取活跃用户统计", description = "获取DAU/MAU活跃用户统计")
    public ActiveUserStatistics getActiveUsers(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // 构造日期范围对象，用于业务层查询
        DateRange range = new DateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return statisticsService.getActiveUsers(range);
    }
    
    /**
     * 获取收入统计数据
     * 
     * 功能说明：
     * - 分析指定时间范围内的游戏收入情况
     * - 提供充值收入、ARPU、ARPPU等关键财务指标
     * - 支持收入趋势分析和收入结构分解
     * 
     * 参数说明：
     * - startDate：统计开始日期，格式：YYYY-MM-DD
     * - endDate：统计结束日期，格式：YYYY-MM-DD
     * 
     * 返回数据：
     * - 总收入：指定期间的总充值收入
     * - ARPU：平均每用户收入（Average Revenue Per User）
     * - ARPPU：平均每付费用户收入（Average Revenue Per Paying User）
     * - 收入趋势：日收入、周收入、月收入趋势
     * - 渠道分析：不同支付渠道的收入占比
     * 
     * 应用场景：
     * - 财务报表和收入预测
     * - 商业化策略效果评估
     * - 投资者汇报和业务决策
     * 
     * @param startDate 统计开始日期
     * @param endDate 统计结束日期
     * @return RevenueStatistics 收入统计数据对象
     */
    @GetMapping("/revenue")
    @Operation(summary = "获取收入统计", description = "获取收入统计数据")
    public RevenueStatistics getRevenue(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // 构造日期范围对象，用于业务层查询
        DateRange range = new DateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return statisticsService.getRevenue(range);
    }
    
    /**
     * 获取用户留存分析数据
     * 
     * 功能说明：
     * - 分析用户在指定天数后的留存情况
     * - 计算留存率并提供留存趋势分析
     * - 用于评估用户粘性和产品吸引力
     * 
     * 参数说明：
     * - days：留存分析的天数，常用值：1、7、30
     * 
     * 返回数据：
     * - 留存率：指定天数的用户留存百分比
     * - 留存趋势：历史留存率的变化趋势
     * - 群组分析：不同用户群组的留存对比
     * - 留存漏斗：用户流失的关键节点分析
     * 
     * 应用场景：
     * - 产品改进和用户体验优化
     * - 新功能上线效果评估
     * - 用户获取成本的ROI分析
     * 
     * @param days 留存分析天数
     * @return RetentionAnalysis 留存分析数据对象
     */
    @GetMapping("/retention/{days}")
    @Operation(summary = "获取留存分析", description = "获取指定天数的留存分析")
    public RetentionAnalysis getRetention(@PathVariable int days) {
        return statisticsService.getRetention(days);
    }
    
    /**
     * 获取付费用户分析数据
     * 
     * 功能说明：
     * - 深度分析付费用户的行为特征和消费模式
     * - 提供付费转化率、付费频次等关键指标
     * - 支持付费用户的分层分析和精准运营
     * 
     * 返回数据：
     * - 付费转化率：免费用户转化为付费用户的比例
     * - 付费用户分布：不同付费金额区间的用户分布
     * - 付费频次：用户的付费次数和付费周期分析
     * - 复购率：付费用户的重复购买行为分析
     * - 付费生命周期：用户从首次付费到流失的生命周期
     * 
     * 应用场景：
     * - 商业化产品的定价策略制定
     * - 付费用户的精准营销和挽留
     * - 游戏经济系统的平衡性调优
     * - VIP系统和会员体系的设计优化
     * 
     * @return PaymentAnalysis 付费分析数据对象
     */
    @GetMapping("/payment-analysis")
    @Operation(summary = "获取付费分析", description = "获取付费用户分析数据")
    public PaymentAnalysis getPaymentAnalysis() {
        return statisticsService.getPaymentAnalysis();
    }
}