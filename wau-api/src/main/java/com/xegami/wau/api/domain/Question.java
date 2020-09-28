package com.xegami.wau.api.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data
@Entity
public class Question extends Base {

    private String body;
    private String comment;
    private boolean hot;

}
