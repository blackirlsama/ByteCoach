package com.shanyangcode.infintechatagent.ai;

import dev.langchain4j.service.SystemMessage;

public interface AiChat {

    @SystemMessage(fromResource = "system-prompt/chat-bot.txt")
    String chat(String prompt);
}
