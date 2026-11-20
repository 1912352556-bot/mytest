package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存实体类
 */
@Data
@TableName("t_inventory")
@Schema(description = "库存实体")
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("dish_id")
    @Schema(description = "商品ID")
    private Long dishId;

    @Schema(description = "当前库存")
    private Integer currentStock;

    @Schema(description = "预警阈值")
    private Integer warningThreshold;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "最后入库时间")
    private LocalDateTime lastInTime;

    @Schema(description = "最后出库时间")
    private LocalDateTime lastOutTime;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

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

    /** 商品名称（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "商品名称")
    private String dishName;

    /** 商品编码（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "商品编码")
    private String dishCode;
}
