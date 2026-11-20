package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生鲜商品实体类
 *
 * <p>业务说明：生鲜即时配送场景下的可售商品（蔬菜、水果、肉禽蛋、水产、乳品烘焙等），
 * 与预制菜相比额外关心保质期、储存温层、产地、规格和损耗率等生鲜属性。</p>
 *
 * <p>注：表名/字段名沿用 dish 前缀，避免存量数据与接口迁移成本。</p>
 */
@Data
@TableName("t_dish")
@Schema(description = "生鲜商品实体")
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "商品名称")
    private String dishName;

    @Schema(description = "商品编码")
    private String dishCode;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "单位：份/盒/kg/袋/瓶")
    private String unit;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "状态：0-下架 1-上架")
    private Integer status;

    // ==================== 生鲜属性 ====================

    @Schema(description = "保质期（天），常温粮油类可填365")
    private Integer shelfLifeDays;

    @Schema(description = "储存温层：1-常温 2-冷藏 3-冷冻")
    private Integer storageType;

    @Schema(description = "产地")
    private String origin;

    @Schema(description = "规格，如 500g/份、30枚/盒")
    private String spec;

    @Schema(description = "损耗率（%），用于库存与采购预估")
    private BigDecimal lossRate;

    /** 当前库存数量（非数据库字段，从t_inventory表关联查询） */
    @TableField(exist = false)
    @Schema(description = "当前库存数量")
    private Integer stock;

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

    /** 分类名称（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "分类名称")
    private String categoryName;
}
