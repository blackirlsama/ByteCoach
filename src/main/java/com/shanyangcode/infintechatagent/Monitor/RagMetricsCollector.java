package com.shanyangcode.infintechatagent.Monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class RagMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    private final ConcurrentMap<String, Counter> hitCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> missCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> retrievalTimersCache = new ConcurrentHashMap<>();

    public void recordHit(String userId, String sessionId) {
        String key = userId + "_" + sessionId;
        Counter counter = hitCountersCache.computeIfAbsent(key, k ->
                Counter.builder("rag_retrieval_hit_total")
                        .tag("user_id", userId)
                        .tag("session_id", sessionId)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    public void recordMiss(String userId, String sessionId) {
        String key = userId + "_" + sessionId;
        Counter counter = missCountersCache.computeIfAbsent(key, k ->
                Counter.builder("rag_retrieval_miss_total")
                        .tag("user_id", userId)
                        .tag("session_id", sessionId)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    public void recordRetrievalTime(String userId, String sessionId, Duration duration) {
        String key = userId + "_" + sessionId;
        Timer timer = retrievalTimersCache.computeIfAbsent(key, k ->
                Timer.builder("rag_retrieval_duration_seconds")
                        .tag("user_id", userId)
                        .tag("session_id", sessionId)
                        .register(meterRegistry)
        );
        timer.record(duration);
    }
}
