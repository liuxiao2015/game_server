package com.game.frame.event.distributed;

import com.game.frame.event.Event;

/**
 * Remote event base class for distributed event processing
 * Contains event metadata for cross-service communication
 *
 * @author lx
 * @date 2024-01-01
 */
public abstract class RemoteEvent extends Event {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String sourceService;
    private String targetService;
    private long publishTime;
    private int retryCount;
    private String traceId;

    public RemoteEvent() {
        super("RemoteEvent", "DistributedEventBus");
        this.eventId = java.util.UUID.randomUUID().toString();
        this.publishTime = System.currentTimeMillis();
        this.retryCount = 0;
    }

    public RemoteEvent(String sourceService, String targetService) {
        super("RemoteEvent", sourceService);
        this.eventId = java.util.UUID.randomUUID().toString();
        this.publishTime = System.currentTimeMillis();
        this.retryCount = 0;
        this.sourceService = sourceService;
        this.targetService = targetService;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * Check if event is targeted to specific service
     */
    public boolean isTargetedTo(String serviceName) {
        return targetService == null || targetService.equals(serviceName) || "*".equals(targetService);
    }

    @Override
    public String toString() {
        return "RemoteEvent{" +
                "eventId='" + eventId + '\'' +
                ", sourceService='" + sourceService + '\'' +
                ", targetService='" + targetService + '\'' +
                ", publishTime=" + publishTime +
                ", retryCount=" + retryCount +
                ", traceId='" + traceId + '\'' +
                "} " + super.toString();
    }
}