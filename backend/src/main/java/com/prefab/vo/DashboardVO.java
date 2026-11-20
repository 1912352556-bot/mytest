package com.prefab.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页统计概览VO
 */
@Data
@Schema(description = "首页统计概览")
public class DashboardVO {

    @Schema(description = "今日订单数")
    private Integer todayOrderCount;

    @Schema(description = "今日完成数")
    private Integer todayCompletedCount;

    @Schema(description = "待调度数")
    private Integer pendingScheduleCount;

    @Schema(description = "配送中数量")
    private Integer deliveringCount;

    @Schema(description = "低库存数量")
    private Integer lowStockCount;

    @Schema(description = "今日订单金额")
    private BigDecimal totalOrderAmount;
}
