package com.shanyangcode.infintechatagent.rag;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Rerank功能验证（启动时自动执行）
 * 设置 rerank.test.enabled=true 启用
 */
@Component
@ConditionalOnProperty(name = "rerank.test.enabled", havingValue = "true")
public class RerankVerifier implements CommandLineRunner {

    private final QwenRerankClient rerankClient;

    public RerankVerifier(QwenRerankClient rerankClient) {
        this.rerankClient = rerankClient;
    }

    @Override
    public void run(String... args) {
        String query = "Java多线程实现方式";
        List<String> docs = Arrays.asList(
                "Java多线程可以通过继承Thread类、实现Runnable接口、实现Callable接口来实现",
                "Python的多线程使用threading模块",
                "线程池是管理线程的一种方式",
                "Java的synchronized关键字用于线程同步"
        );

        List<Integer> result = rerankClient.rerank(query, docs, 2);

        if (result != null && !result.isEmpty()) {
            for (int i = 0; i < result.size(); i++) {
                int idx = result.get(i);
                if (idx >= 0 && idx < docs.size()) { // 增加索引校验
                } else {
                }
            }
        } else {
        }
    }
}