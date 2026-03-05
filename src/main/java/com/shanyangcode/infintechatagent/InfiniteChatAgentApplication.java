package com.shanyangcode.infintechatagent;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class InfiniteChatAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfiniteChatAgentApplication.class, args);
    }

}
