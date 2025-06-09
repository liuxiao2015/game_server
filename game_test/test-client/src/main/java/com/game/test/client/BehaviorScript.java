package com.game.test.client;

import java.util.List;

/**
 * 行为脚本
 * @author lx
 * @date 2025/06/08
 */
public class BehaviorScript {
    
    private String name;
    private String description;
    private List<BehaviorStep> steps;
    
    public BehaviorScript(String name, String description, List<BehaviorStep> steps) {
        this.name = name;
        this.description = description;
        this.steps = steps;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<BehaviorStep> getSteps() { return steps; }
}

/**
 * 行为步骤
 */
class BehaviorStep {
    
    private String action;
    private String parameters;
    private long delayMs;
    
    public BehaviorStep(String action, String parameters, long delayMs) {
        this.action = action;
        this.parameters = parameters;
        this.delayMs = delayMs;
    }
    
    // Getters
    public String getAction() { return action; }
    public String getParameters() { return parameters; }
    public long getDelayMs() { return delayMs; }
}