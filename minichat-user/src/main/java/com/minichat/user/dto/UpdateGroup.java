package com.minichat.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroup {
    @Size(max = 50, message = "群名称最长 50 个字符")
    private String name;

    @Size(max = 500, message = "公告最长 500 个字符")
    private String notice;

}
