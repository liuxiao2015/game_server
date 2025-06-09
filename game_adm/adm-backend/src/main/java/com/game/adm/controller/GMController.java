package com.game.adm.controller;

import com.game.adm.gm.GMService;
import com.game.adm.gm.GMService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * GM工具控制器
 * @author lx
 * @date 2025/06/08
 */
@RestController
@RequestMapping("/api/adm/gm")
@Tag(name = "GM工具", description = "游戏管理员工具相关接口")
public class GMController {
    
    @Autowired
    private GMService gmService;
    
    @PostMapping("/players/{playerId}/ban")
    @Operation(summary = "封禁玩家", description = "封禁指定玩家账号")
    public void banPlayer(
            @PathVariable Long playerId,
            @RequestParam String reason,
            @RequestParam int durationHours) {
        gmService.banPlayer(playerId, reason, Duration.ofHours(durationHours));
    }
    
    @PostMapping("/players/{playerId}/kick")
    @Operation(summary = "踢出玩家", description = "踢出指定玩家")
    public void kickPlayer(@PathVariable Long playerId) {
        gmService.kickPlayer(playerId);
    }
    
    @GetMapping("/players/search")
    @Operation(summary = "查询玩家", description = "根据关键词查询玩家信息")
    public PlayerInfo queryPlayer(@RequestParam String keyword) {
        return gmService.queryPlayer(keyword);
    }
    
    @PostMapping("/players/{playerId}/items")
    @Operation(summary = "发放物品", description = "向玩家发放指定物品")
    public void sendItems(
            @PathVariable Long playerId,
            @RequestBody List<ItemInfo> items) {
        gmService.sendItems(playerId, items);
    }
    
    @PostMapping("/mail")
    @Operation(summary = "发送邮件", description = "向玩家发送邮件")
    public void sendMail(@RequestBody MailRequest request) {
        gmService.sendMail(request);
    }
    
    @PostMapping("/broadcast")
    @Operation(summary = "广播消息", description = "向所有在线玩家广播消息")
    public void broadcast(@RequestParam String message) {
        gmService.broadcast(message);
    }
    
    @PostMapping("/config/reload")
    @Operation(summary = "重载配置", description = "重新加载服务器配置")
    public void reloadConfig(@RequestParam String configType) {
        gmService.reloadConfig(configType);
    }
    
    @PostMapping("/script/execute")
    @Operation(summary = "执行脚本", description = "执行GM脚本命令")
    public void executeScript(@RequestParam String script) {
        if (!isValidScript(script)) {
            throw new IllegalArgumentException("Invalid script format.");
        }
        gmService.executeScript(script);
    }
    
    private boolean isValidScript(String script) {
        // Example validation: only allow alphanumeric characters and basic operators
        String scriptPattern = "^[a-zA-Z0-9_\\-+*/\\s]+$";
        return script != null && script.matches(scriptPattern);
    }
}