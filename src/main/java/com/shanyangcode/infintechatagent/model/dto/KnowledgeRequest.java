package com.shanyangcode.infintechatagent.model.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class KnowledgeRequest implements Serializable {


    /**
     * 问题，例如：这个软件叫什么名字？
     */
    private String question;

    /**
     * 答案，例如：本软件名为「千言」...
     */
    private String answer;

    /**
     * (可选) 来源名称，用于模拟 file_name
     */
    private String sourceName;

}

