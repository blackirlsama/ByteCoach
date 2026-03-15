package com.shanyangcode.infintechatagent.rag;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QueryExpander {

    private final ChatLanguageModel chatModel;

    public QueryExpander(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public String expand(String originalQuery) {
        if (originalQuery == null || originalQuery.length() < 5) {
            return originalQuery;
        }

        try {
            String prompt = String.format(
                "将以下查询扩展为3个相关的搜索关键词，用空格分隔，不要解释：\n%s",
                originalQuery
            );

            String expanded = chatModel.generate(prompt);
            String result = originalQuery + " " + expanded.trim();
            log.info("查询扩展: {} -> {}", originalQuery, result);
            return result;
        } catch (Exception e) {
            log.warn("查询扩展失败: {}", e.getMessage());
            return originalQuery;
        }
    }
}
