package com.shanyangcode.infintechatagent.Monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;




@Component
@Slf4j
public class AiModelMonitorListener implements ChatModelListener {

    // 定义一个 Key，用于在请求和响应之间传递开始时间
    private static final String START_TIME_KEY = "request_start_time";

    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    @Resource
    private ObservabilityLogger observabilityLogger;


    @Override
    public void onRequest(ChatModelRequestContext requestContext) {

        requestContext.attributes().put(START_TIME_KEY, Instant.now());
        MonitorContext context = MonitorContextHolder.getContext();

        if (context == null) {
            log.error("MonitorContext is null when processing request");
            return;
        }
        String userId = context.getUserId() != null ? context.getUserId().toString() : "unknown";
        String sessionId = context.getSessionId() != null ? context.getSessionId().toString() : "unknown";
        requestContext.attributes().put(MONITOR_CONTEXT_KEY, context);
        String modelName = requestContext.chatRequest().modelName();

        observabilityLogger.logRequest("LLM_REQUEST", "model", modelName);
        aiModelMetricsCollector.recordRequest(userId, sessionId, modelName, "started");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        String modelName = responseContext.chatResponse().metadata().modelName();
        Map<Object, Object> attributes = responseContext.attributes();
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);

        if (context == null) {
            log.warn("监控上下文丢失，无法记录响应指标 - Model: {}", responseContext.chatResponse().modelName());
            return;
        }

        String userId = context.getUserId().toString();
        String sessionId = context.getSessionId().toString();
        Duration durationMs = calculateDuration(attributes);
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();

        observabilityLogger.logSuccess("LLM_RESPONSE", durationMs.toMillis(),
            "model", modelName,
            "input_tokens", tokenUsage != null ? tokenUsage.inputTokenCount() : 0,
            "output_tokens", tokenUsage != null ? tokenUsage.outputTokenCount() : 0);

        aiModelMetricsCollector.recordRequest(userId, sessionId, modelName, "success");
        aiModelMetricsCollector.recordResponseTime(userId, sessionId, modelName, durationMs);

        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, sessionId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, sessionId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, sessionId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        MonitorContext context = MonitorContextHolder.getContext();
        Map<Object, Object> attributes = errorContext.attributes();
        Duration durationMs = calculateDuration(attributes);

        if (context == null) {
            context = (MonitorContext) errorContext.attributes().get(MONITOR_CONTEXT_KEY);
        }

        if (context == null) {
            log.warn("监控上下文丢失，无法记录错误指标 - Error: {}", errorContext.error().getMessage());
            return;
        }

        String userId = context.getUserId().toString();
        String sessionId = context.getSessionId().toString();
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();

        observabilityLogger.logError("LLM_ERROR", durationMs.toMillis(), errorMessage);

        aiModelMetricsCollector.recordRequest(userId, sessionId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, sessionId, modelName, errorMessage);
        aiModelMetricsCollector.recordResponseTime(userId, sessionId, modelName, durationMs);
    }

    /**
     * 从 attributes 中取出开始时间并计算间隔
     */
    private Duration calculateDuration(Map<Object, Object> attributes) {
        Instant startTime = (Instant) attributes.get(START_TIME_KEY);
        if (startTime != null) {
            return Duration.between(startTime, Instant.now());
        }
        return Duration.ZERO;
    }
}