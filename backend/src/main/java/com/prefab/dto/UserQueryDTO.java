package com.prefab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
@Schema(description = "用户查询参数")
public class UserQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realname;

    @Schema(description = "角色")
    private Integer role;

    @Schema(description = "手机号")
    private String phone;
}
