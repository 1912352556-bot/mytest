package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 */
@Data
@TableName("t_order_item")
@Schema(description = "订单明细实体")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;

    @TableField("dish_id")
    @Schema(description = "商品ID")
    private Long dishId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 商品名称（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "商品名称")
    private String dishName;

    /** 商品编码（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "商品编码")
    private String dishCode;
}
