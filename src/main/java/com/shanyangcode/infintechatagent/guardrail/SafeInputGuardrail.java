package com.shanyangcode.infintechatagent.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;

public class SafeInputGuardrail implements InputGuardrail {


    private static final Set<String> sensitiveWords = Set.of("死", "杀");

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String inputText = userMessage.singleText();


        for (String keyword : sensitiveWords) {
            if (!keyword.isEmpty() && inputText.contains(keyword)) {
                return fatal("提问不能包含敏感词！！！！！");
            }
        }

        return success();
    }
}