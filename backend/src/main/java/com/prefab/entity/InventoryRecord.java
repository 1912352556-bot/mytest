package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变更记录实体类
 */
@Data
@TableName("t_inventory_record")
@Schema(description = "库存变更记录实体")
public class InventoryRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("inventory_id")
    @Schema(description = "库存ID")
    private Long inventoryId;

    @Schema(description = "变更类型：1-入库 2-出库")
    private Integer changeType;

    @Schema(description = "变更数量")
    @TableField("change_quantity")
    private Integer quantity;

    @Schema(description = "变更前库存")
    private Integer beforeStock;

    @Schema(description = "变更后库存")
    private Integer afterStock;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 商品名称（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "商品名称")
    private String dishName;

    /** 订单编号（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "订单编号")
    private String orderNo;

    /** 操作人姓名（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "操作人姓名")
    private String operatorName;
}
