package com.game.frame.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Event listener annotation for marking event handler methods
 * Supports event type specification, priority, and async marking
 *
 * @author lx
 * @date 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @/**
 * EventListener
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
interface EventListener {
    
    /**
     * Event types this listener handles
     * If empty, will handle all event types the method parameter supports
     * 
     * @return array of event type names
     */
    String[] eventTypes() default {};
    
    /**
     * Priority of this event listener
     * Higher numbers indicate higher priority (executed first)
     * 
     * @return priority value
     */
    int priority() default 0;
    
    /**
     * Whether this listener should be executed asynchronously
     * 
     * @return true for async execution
     */
    boolean async() default true;
    
    /**
     * Whether this listener can handle events from the same thread
     * that published the event
     * 
     * @return true to allow same-thread handling
     */
    boolean allowSameThread() default true;
}