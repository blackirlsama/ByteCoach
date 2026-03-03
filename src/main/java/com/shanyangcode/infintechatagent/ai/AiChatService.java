package com.shanyangcode.infintechatagent.ai;

import java.util.List;



import com.shanyangcode.infintechatagent.ai.AiChat;
import com.shanyangcode.infintechatagent.tool.TimeTool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Bean
    public AiChat aiChat() {
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        return AiServices.builder(AiChat.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .tools(new TimeTool())
                .toolProvider(mcpToolProvider)
                .build();
    }
}