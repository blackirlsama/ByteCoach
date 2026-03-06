package com.shanyangcode.infintechatagent.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RagTool {

    @Resource
    private EmbeddingStoreIngestor embeddingStoreIngestor;

    @Value("${rag.docs-path}")
    private String docsPath;

    /**
     *  定义工具方法。
     * 大模型会根据 @Tool 的描述和参数名来决定何时调用。
     */
    @Tool("当用户想要保存问答对、知识点或者向知识库添加新信息时调用此工具。将问题、答案和目标文件名作为参数。")
    public String addKnowledgeToRag(String question, String answer, String fileName) {
        log.info("Tool 调用: 正在保存知识 - Q: {}, file: {}", question, fileName);

        // 1. 格式化内容
        String formattedContent = String.format("### Q：%s\n\nA：%s", question, answer);

        // 2. 处理文件名 (防止没写后缀)
        if (fileName == null || fileName.isBlank()) {
            fileName = "InfiniteChat.md"; // 默认文件
        }
        if (!fileName.endsWith(".md")) {
            fileName = fileName + ".md";
        }

        // 3. 写入物理文件
        boolean writeSuccess = appendToFile(formattedContent, fileName);
        if (!writeSuccess) {
            return "保存失败：无法写入本地文件系统，请检查日志。";
        }

        // 4. 存入向量数据库
        try {
            // 设置来源元数据
            Metadata metadata = Metadata.from("file_name", fileName);

            // 创建文档并 Embedding
            Document document = Document.from(formattedContent, metadata);
            embeddingStoreIngestor.ingest(document);

            log.info("Tool 执行成功: 知识已同步至 RAG");
            return "成功！已将该知识点保存到文档 [" + fileName + "] 并同步至向量数据库。";
        } catch (Exception e) {
            log.error("RAG - 向量化失败", e);
            return "文件写入成功，但向量数据库更新失败：" + e.getMessage();
        }
    }

    /**
     * 辅助方法：追加写入文件
     */
    private synchronized boolean appendToFile(String content, String fileName) {
        try {
            Path filePath = Paths.get(docsPath, fileName);
            
            // 如果文件不存在，先创建
            if (!Files.exists(filePath)) {
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.createFile(filePath);
                log.info("Tool created new file: {}", filePath.toAbsolutePath());
            }

            // 前后加换行符
            String textToAppend = "\n\n" + content;

            Files.writeString(
                    filePath,
                    textToAppend,
                    StandardOpenOption.APPEND
            );
            return true;
        } catch (IOException e) {
            log.error("RAG Tool - 写入文件失败: {}", e.getMessage(), e);
            return false;
        }
    }
}