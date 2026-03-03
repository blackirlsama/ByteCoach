package com.shanyangcode.infintechatagent.ai;

import com.shanyangcode.infintechatagent.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;


@InputGuardrails({SafeInputGuardrail.class})
public interface AiChat {

    @SystemMessage(fromResource = "system-prompt/chat-bot.txt")
    String chat(@MemoryId String sessionId, @UserMessage String prompt);
}
