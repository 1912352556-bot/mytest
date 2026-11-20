package com.prefab.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "是否成功")
    private Boolean flag;

    @Schema(description = "消息")
    private String msg;

    @Schema(description = "数据")
    private T data;

    private Result() {}

    private Result(Integer code, Boolean flag, String msg, T data) {
        this.code = code;
        this.flag = flag;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), true, ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), true, ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), true, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.FAIL.getCode(), false, ResultCode.FAIL.getMsg(), null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), false, msg, null);
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, false, msg, null);
    }

    /**
     * 失败响应（使用ResultCode）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), false, resultCode.getMsg(), null);
    }

    /**
     * 自定义响应
     */
    public static <T> Result<T> result(Integer code, Boolean flag, String msg, T data) {
        return new Result<>(code, flag, msg, data);
    }
}
