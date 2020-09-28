package com.xegami.wau.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionDTO extends BaseDTO {

    private String body;
    private String comment;
    private boolean hot;

}
