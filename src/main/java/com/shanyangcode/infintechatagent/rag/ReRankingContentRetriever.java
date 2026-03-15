package com.shanyangcode.infintechatagent.rag;

import com.shanyangcode.infintechatagent.Monitor.MonitorContext;
import com.shanyangcode.infintechatagent.Monitor.MonitorContextHolder;
import com.shanyangcode.infintechatagent.Monitor.ObservabilityLogger;
import com.shanyangcode.infintechatagent.Monitor.RagMetricsCollector;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ReRankingContentRetriever implements ContentRetriever {

    private final ContentRetriever baseRetriever;
    private final QwenRerankClient rerankClient;
    private final int finalTopN;
    private final QueryPreprocessor queryPreprocessor;
    private final RagMetricsCollector ragMetricsCollector;
    private final ObservabilityLogger observabilityLogger;

    public ReRankingContentRetriever(ContentRetriever baseRetriever,
                                     QwenRerankClient rerankClient,
                                     int finalTopN,
                                     QueryPreprocessor queryPreprocessor,
                                     RagMetricsCollector ragMetricsCollector,
                                     ObservabilityLogger observabilityLogger) {
        this.baseRetriever = Objects.requireNonNull(baseRetriever, "baseRetriever不能为空");
        this.rerankClient = Objects.requireNonNull(rerankClient, "rerankClient不能为空");
        this.finalTopN = finalTopN > 0 ? finalTopN : 3;
        this.queryPreprocessor = queryPreprocessor;
        this.ragMetricsCollector = ragMetricsCollector;
        this.observabilityLogger = observabilityLogger;
    }

    @Override
    public List<Content> retrieve(Query query) {
        Instant startTime = Instant.now();
        MonitorContext context = MonitorContextHolder.getContext();
        String userId = context != null && context.getUserId() != null ? context.getUserId().toString() : "unknown";
        String sessionId = context != null && context.getSessionId() != null ? context.getSessionId().toString() : "unknown";

        if (query == null || query.text() == null || query.text().isBlank()) {
            log.warn("⚠️ [RAG检索] 查询语句为空，直接返回空结果");
            if (ragMetricsCollector != null) ragMetricsCollector.recordMiss(userId, sessionId);
            return List.of();
        }

        String originalQuery = query.text();
        String processedQuery = originalQuery;

        if (queryPreprocessor != null) {
            processedQuery = queryPreprocessor.preprocess(originalQuery);
            query = Query.from(processedQuery);
        }

        if (observabilityLogger != null) {
            observabilityLogger.logRequest("RAG_RETRIEVAL", "query", processedQuery.substring(0, Math.min(50, processedQuery.length())));
        }

        List<Content> candidates = baseRetriever.retrieve(query);

        if (candidates == null || candidates.isEmpty()) {
            log.warn("⚠️ [阶段1-粗排] 向量召回无结果");
            if (ragMetricsCollector != null) ragMetricsCollector.recordMiss(userId, sessionId);
            recordMetrics(startTime, userId, sessionId, 0);
            return List.of();
        }

        log.info("✅ [阶段1-粗排] 召回 {} 条候选文档", candidates.size());

        if (candidates.size() <= finalTopN) {
            if (ragMetricsCollector != null) ragMetricsCollector.recordHit(userId, sessionId);
            recordMetrics(startTime, userId, sessionId, candidates.size());
            return new ArrayList<>(candidates);
        }

        // 第二阶段：Rerank精排
        log.info("🎯 [阶段2-精排] 开始Rerank重排序 ({} -> Top{})", candidates.size(), finalTopN);

        try {
            List<String> documents = candidates.stream()
                    .map(Content::textSegment)
                    .map(TextSegment::text)
                    .collect(Collectors.toList());

            List<Integer> rerankIndices = rerankClient.rerank(query.text(), documents, finalTopN);

            if (rerankIndices == null || rerankIndices.isEmpty()) {
                log.warn("⚠️ [降级处理] Rerank失败，使用原始向量检索Top{}", finalTopN);
                List<Content> fallbackResults = new ArrayList<>(candidates.subList(0, Math.min(finalTopN, candidates.size())));
                if (ragMetricsCollector != null) ragMetricsCollector.recordHit(userId, sessionId);
                recordMetrics(startTime, userId, sessionId, fallbackResults.size());
                return fallbackResults;
            }

            List<Content> rerankedResults = new ArrayList<>();
            for (Integer index : rerankIndices) {
                if (index >= 0 && index < candidates.size()) {
                    rerankedResults.add(candidates.get(index));
                } else {
                    log.warn("⚠️ [Rerank] 无效索引{}，跳过", index);
                }
            }

            if (rerankedResults.isEmpty()) {
                log.warn("⚠️ [Rerank] 重排序后无有效结果，降级为原始Top{}", finalTopN);
                rerankedResults = new ArrayList<>(candidates.subList(0, Math.min(finalTopN, candidates.size())));
            }

            log.info("✅ [阶段2-精排] Rerank完成 - 最终返回 {} 条精排结果", rerankedResults.size());
            if (ragMetricsCollector != null) ragMetricsCollector.recordHit(userId, sessionId);
            recordMetrics(startTime, userId, sessionId, rerankedResults.size());
            return rerankedResults;

        } catch (Exception e) {
            log.error("❌ [异常处理] Rerank处理异常，降级为原始检索", e);
            List<Content> fallbackResults = new ArrayList<>(candidates.subList(0, Math.min(finalTopN, candidates.size())));
            if (ragMetricsCollector != null) ragMetricsCollector.recordHit(userId, sessionId);
            recordMetrics(startTime, userId, sessionId, fallbackResults.size());
            return fallbackResults;
        }
    }

    private void recordMetrics(Instant startTime, String userId, String sessionId, int resultCount) {
        Duration duration = Duration.between(startTime, Instant.now());
        if (ragMetricsCollector != null) {
            ragMetricsCollector.recordRetrievalTime(userId, sessionId, duration);
        }
        if (observabilityLogger != null) {
            observabilityLogger.logSuccess("RAG_RETRIEVAL", duration.toMillis(), "result_count", resultCount);
        }
    }
}