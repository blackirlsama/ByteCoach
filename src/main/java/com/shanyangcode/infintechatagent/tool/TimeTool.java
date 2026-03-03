package com.shanyangcode.infintechatagent.tool;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import dev.langchain4j.agent.tool.Tool;

public class TimeTool {

    @Tool("getCurrentTime")
    public String getCurrentTimeInShanghai() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss EEEE (中国标准时间)"));
    }
}