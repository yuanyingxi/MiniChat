package com.minichat.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupReq {

    private String groupName;

    private List<Long> memberIds;
}
