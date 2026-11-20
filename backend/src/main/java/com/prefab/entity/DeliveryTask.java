package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配送任务实体类
 */
@Data
@TableName("t_delivery_task")
@Schema(description = "配送任务实体")
public class DeliveryTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "配送单号")
    private String deliveryNo;

    @Schema(description = "调度任务ID")
    private Long scheduleTaskId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "配送人员ID")
    private Long deliveryPersonId;

    @Schema(description = "配送地址")
    private String deliveryAddress;

    @Schema(description = "配送状态：0-待取货 1-配送中 2-已送达 3-异常")
    private Integer deliveryStatus;

    @Schema(description = "取货时间")
    private LocalDateTime pickupTime;

    @Schema(description = "送达时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "逻辑删除")
    private Integer deleted;

    /** 订单编号（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "订单编号")
    private String orderNo;

    /** 配送人员姓名（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "配送人员姓名")
    private String deliveryPersonName;

    /** 客户姓名（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "客户姓名")
    private String customerName;

    /** 客户电话（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "客户电话")
    private String customerPhone;

    /** 期望配送时间（非数据库字段，来自订单） */
    @TableField(exist = false)
    @Schema(description = "期望配送时间")
    private java.time.LocalDateTime expectedDeliveryTime;

    /** 订单状态（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "订单状态：0-待调度 1-已调度 2-配送中 3-已完成 4-已取消")
    private Integer orderStatus;

    /** 订单金额（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "订单金额")
    private java.math.BigDecimal totalAmount;

    /** 配送地址经度（非数据库字段，来自订单） */
    @TableField(exist = false)
    @Schema(description = "配送地址经度")
    private java.math.BigDecimal longitude;

    /** 配送地址纬度（非数据库字段，来自订单） */
    @TableField(exist = false)
    @Schema(description = "配送地址纬度")
    private java.math.BigDecimal latitude;
}
