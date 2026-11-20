package com.prefab.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // 参数错误 4xx
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 业务错误 5xx
    USER_NOT_FOUND(5001, "用户不存在"),
    USER_PASSWORD_ERROR(5002, "用户名或密码错误"),
    USER_DISABLED(5003, "用户已被禁用"),
    USER_EXISTS(5004, "用户名已存在"),
    
    ORDER_NOT_FOUND(5101, "订单不存在"),
    ORDER_STATUS_ERROR(5102, "订单状态不正确"),
    ORDER_CANNOT_CANCEL(5103, "订单无法取消"),
    
    SCHEDULE_NOT_FOUND(5201, "调度任务不存在"),
    SCHEDULE_ALREADY_ASSIGNED(5202, "调度任务已分配"),
    
    DELIVERY_NOT_FOUND(5301, "配送任务不存在"),
    DELIVERY_PERSON_BUSY(5302, "配送人员正在配送中"),
    
    INVENTORY_NOT_FOUND(5401, "库存记录不存在"),
    INVENTORY_NOT_ENOUGH(5402, "库存不足"),
    STOCK_NOT_ENOUGH(5403, "商品库存不足"),
    
    DISH_NOT_FOUND(5501, "商品不存在"),
    DISH_CODE_EXISTS(5502, "商品编码已存在");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
