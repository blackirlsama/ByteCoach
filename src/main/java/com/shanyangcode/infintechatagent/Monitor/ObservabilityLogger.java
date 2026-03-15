package com.shanyangcode.infintechatagent.Monitor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ObservabilityLogger {

    public void logRequest(String action, Object... params) {
        MonitorContext ctx = MonitorContextHolder.getContext();
        if (ctx != null) {
            MDC.put("request_id", ctx.getRequestId());
            MDC.put("session_id", String.valueOf(ctx.getSessionId()));
            MDC.put("user_id", String.valueOf(ctx.getUserId()));
        }
        log.info("[{}] {}", action, formatParams(params));
    }

    public void logSuccess(String action, long duration, Object... params) {
        log.info("[{}] SUCCESS duration={}ms {}", action, duration, formatParams(params));
        MDC.clear();
    }

    public void logError(String action, long duration, String error) {
        log.error("[{}] ERROR duration={}ms error={}", action, duration, error);
        MDC.clear();
    }

    private String formatParams(Object... params) {
        if (params == null || params.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                sb.append(params[i]).append("=").append(params[i + 1]).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
