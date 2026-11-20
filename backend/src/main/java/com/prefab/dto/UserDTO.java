package com.prefab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户DTO
 */
@Data
@Schema(description = "用户请求参数")
public class UserDTO {

    @Schema(description = "用户ID（更新时必填）")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别：0-未知 1-男 2-女")
    private Integer gender;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色：0-管理员 1-调度员 2-配送员")
    private Integer role;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
}
