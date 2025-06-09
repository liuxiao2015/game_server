package com.game.adm.controller;

import com.game.adm.statistics.StatisticsService;
import com.game.adm.statistics.StatisticsService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 统计数据控制器
 * @author lx
 * @date 2025/06/08
 */
@RestController
@RequestMapping("/api/adm/statistics")
@Tag(name = "统计数据", description = "游戏统计数据相关接口")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @GetMapping("/online")
    @Operation(summary = "获取实时在线统计", description = "获取当前在线人数和峰值统计")
    public OnlineStatistics getOnlineStatistics() {
        return statisticsService.getRealtimeOnline();
    }
    
    @GetMapping("/active-users")
    @Operation(summary = "获取活跃用户统计", description = "获取DAU/MAU活跃用户统计")
    public ActiveUserStatistics getActiveUsers(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        DateRange range = new DateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return statisticsService.getActiveUsers(range);
    }
    
    @GetMapping("/revenue")
    @Operation(summary = "获取收入统计", description = "获取收入统计数据")
    public RevenueStatistics getRevenue(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        DateRange range = new DateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return statisticsService.getRevenue(range);
    }
    
    @GetMapping("/retention/{days}")
    @Operation(summary = "获取留存分析", description = "获取指定天数的留存分析")
    public RetentionAnalysis getRetention(@PathVariable int days) {
        return statisticsService.getRetention(days);
    }
    
    @GetMapping("/payment-analysis")
    @Operation(summary = "获取付费分析", description = "获取付费用户分析数据")
    public PaymentAnalysis getPaymentAnalysis() {
        return statisticsService.getPaymentAnalysis();
    }
}