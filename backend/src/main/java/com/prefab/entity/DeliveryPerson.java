package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配送人员实体类
 */
@Data
@TableName("t_delivery_person")
@Schema(description = "配送人员实体")
public class DeliveryPerson implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "姓名")
    private String personName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "状态：0-空闲 1-配送中 2-休息")
    private Integer status;

    @Schema(description = "当前任务数")
    private Integer currentTaskCount;

    @Schema(description = "总配送次数")
    private Integer totalDeliveryCount;

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
