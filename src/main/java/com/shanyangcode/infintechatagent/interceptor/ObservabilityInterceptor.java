package com.shanyangcode.infintechatagent.interceptor;

import com.shanyangcode.infintechatagent.Monitor.MonitorContext;
import com.shanyangcode.infintechatagent.Monitor.MonitorContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class ObservabilityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        MonitorContext context = MonitorContextHolder.getContext();

        if (context == null) {
            context = MonitorContext.builder()
                    .requestId(requestId)
                    .startTime(System.currentTimeMillis())
                    .build();
        } else {
            context.setRequestId(requestId);
            context.setStartTime(System.currentTimeMillis());
        }

        MonitorContextHolder.setContext(context);
        response.setHeader("X-Request-ID", requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MonitorContextHolder.clearContext();
    }
}
