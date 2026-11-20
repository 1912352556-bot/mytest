package com.prefab.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调度任务实体类
 */
@Data
@TableName("t_schedule_task")
@Schema(description = "调度任务实体")
public class ScheduleTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "配送区域")
    private String deliveryArea;

    @Schema(description = "期望配送时间")
    private LocalDateTime expectedDeliveryTime;

    @Schema(description = "指派人ID（配送员）")
    private Long assigneeId;

    @Schema(description = "任务状态：0-待分配 1-已分配 2-执行中 3-已完成")
    private Integer taskStatus;

    @Schema(description = "优先级：0-低 1-中 2-高 3-紧急")
    private Integer priority;

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

    /** 指派人姓名（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "指派人姓名")
    private String assigneeName;
}
