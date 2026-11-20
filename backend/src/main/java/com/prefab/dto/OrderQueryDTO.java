package com.prefab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单查询DTO
 */
@Data
@Schema(description = "订单查询参数")
public class OrderQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "配送区域")
    private String deliveryArea;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
