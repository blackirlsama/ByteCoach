package com.shanyangcode.infintechatagent.controller;


import com.shanyangcode.infintechatagent.Monitor.MonitorContext;
import com.shanyangcode.infintechatagent.Monitor.MonitorContextHolder;
import com.shanyangcode.infintechatagent.ai.AiChat;
import com.shanyangcode.infintechatagent.model.dto.ChatRequest;
import com.shanyangcode.infintechatagent.model.dto.KnowledgeRequest;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@Slf4j
public class AiChatController {

    @Resource
    private AiChat aiChat;


    @Resource
    private EmbeddingStoreIngestor embeddingStoreIngestor;

    @Value("${rag.docs-path}")
    private String docsPath;

    private final  String  TARGET_FILENAME = "InfiniteChat.md";

//    @GetMapping("/chat")
//    public String chat(String sessionId, String prompt) {
//        return aiChat.chat(sessionId, prompt);
//    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest chatRequest) {
        MonitorContextHolder.setContext(MonitorContext.builder().userId(chatRequest.getUserId()).sessionId(chatRequest.getSessionId()).build());
        String chat = aiChat.chat(chatRequest.getSessionId(), chatRequest.getPrompt());
        MonitorContextHolder.clearContext();
        return chat;
    }


//    @PostMapping("/streamChat")
//    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
//        return aiChat.streamChat(chatRequest.getSessionId(), chatRequest.getPrompt());
//    }




    @PostMapping("/streamChat")
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
        MonitorContext context = MonitorContext.builder()
                .userId(chatRequest.getUserId())
                .sessionId(chatRequest.getSessionId())
                .build();

        return Flux.defer(() -> {
            MonitorContextHolder.setContext(context);
            return aiChat.streamChat(chatRequest.getSessionId(), chatRequest.getPrompt())
                    .doFinally(signal -> MonitorContextHolder.clearContext());
        });
    }



    @PostMapping("/insert")
    public String insertKnowledge(@RequestBody KnowledgeRequest knowledgeRequest) {
        // 1. 格式化内容
        String formattedContent = String.format("### Q：%s\n\nA：%s", knowledgeRequest.getQuestion(), knowledgeRequest.getAnswer());

        // 2. 写入物理文件 (InfiniteChat.md)
        boolean writeSuccess = appendToFile(formattedContent, knowledgeRequest.getSourceName());
        if (!writeSuccess) {
            return "插入失败：无法写入本地文件";
        }

        // 3. 存入向量数据库 (RAG)
        try {
            // 设置来源元数据
            String sourceName = (knowledgeRequest.getSourceName() != null) ? knowledgeRequest.getSourceName() : TARGET_FILENAME;
            Metadata metadata = Metadata.from("file_name", sourceName);

            // 创建文档并 Embedding
            Document document = Document.from(formattedContent, metadata);
            embeddingStoreIngestor.ingest(document);

            log.info("RAG - 新增知识点成功: {}", knowledgeRequest.getQuestion());
            return "插入成功：已同步至 " + knowledgeRequest.getSourceName() + " 及向量数据库";
        } catch (Exception e) {
            log.error("RAG - 向量化失败", e);
            return "插入部分成功：文件已写入，但向量库更新失败";
        }
    }



    private synchronized boolean appendToFile(String content, String sourceName) {
        try {
            // 拼接完整路径
            Path filePath = Paths.get(docsPath, sourceName);
            log.info("文件实际写入位置: {}", filePath.toAbsolutePath());
            // 如果文件不存在，先创建
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }

            // 准备要写入的文本，前后加换行符确保格式独立
            String textToAppend = "\n\n" + content;

            // 执行追加写入
            Files.writeString(
                    filePath,
                    textToAppend,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );
            return true;
        } catch (IOException e) {
            log.error("RAG - 写入本地文件失败: {}", e.getMessage(), e);
            return false;
        }
    }
}



