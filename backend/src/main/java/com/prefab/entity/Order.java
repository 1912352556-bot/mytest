package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@TableName("t_order")
@Schema(description = "订单实体")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "客户电话")
    private String customerPhone;

    @Schema(description = "配送地址")
    private String deliveryAddress;

    @Schema(description = "配送区域")
    private String deliveryArea;

    @Schema(description = "配送地址经度（地图选点）")
    private BigDecimal longitude;

    @Schema(description = "配送地址纬度（地图选点）")
    private BigDecimal latitude;

    @Schema(description = "期望配送时间")
    private LocalDateTime expectedDeliveryTime;

    @Schema(description = "实际配送时间")
    private LocalDateTime actualDeliveryTime;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态：0-待调度 1-已调度 2-配送中 3-已完成 4-已取消")
    private Integer orderStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long creatorId;

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

    /** 订单明细（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "订单明细")
    private List<OrderItem> orderItems;

    /** 创建人姓名（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "创建人姓名")
    private String creatorName;
}
