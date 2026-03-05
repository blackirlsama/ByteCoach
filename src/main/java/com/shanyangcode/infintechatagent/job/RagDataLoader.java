package com.shanyangcode.infintechatagent.job;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 自动加载文档
 */
@Component
@Slf4j
public class RagDataLoader implements CommandLineRunner {

    @Value("${rag.docs-path}")
    private String docsPath;

    @Resource
    private EmbeddingStoreIngestor embeddingStoreIngestor;

    @Override
    public void run(String... args) {
        log.info("RAG - 开始加载本地基础文档，路径: {}", docsPath);
        try {

            List<Document> documents = FileSystemDocumentLoader.loadDocuments(docsPath);

            if (!documents.isEmpty()) {
                embeddingStoreIngestor.ingest(documents);
                log.info("RAG - 本地文档加载完成，共加载 {} 个文档", documents.size());
            } else {
                log.warn("RAG - 指定路径下未发现文档");
            }
        } catch (Exception e) {
            log.error("RAG - 加载本地文档失败", e);
        }
    }
}