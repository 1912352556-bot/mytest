package com.prefab.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应VO
 */
@Data
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realname;

    @Schema(description = "角色")
    private Integer role;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "Token")
    private String token;
}
