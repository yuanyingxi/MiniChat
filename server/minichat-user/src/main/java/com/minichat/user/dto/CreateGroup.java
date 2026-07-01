package com.minichat.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroup {
    @NotBlank(message = "群名称不能为空")
    @Size(max = 50, message = "群名称最长 50 个字符")
    private String name;

    @NotEmpty(message = "请选择群成员")
    private List<Long> memberIds;   // 选中的群成员 ID（不含自己）

}
