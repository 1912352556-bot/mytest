package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生鲜商品分类实体类（蔬菜、水果、肉禽蛋、水产海鲜、乳品烘焙、粮油调味等）
 */
@Data
@TableName("t_dish_category")
@Schema(description = "生鲜商品分类实体")
public class DishCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "排序")
    private Integer sortOrder;

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
}
