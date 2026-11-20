package com.prefab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO
 */
@Data
@Schema(description = "订单请求参数")
public class OrderDTO {

    @Schema(description = "订单ID（更新时必填）")
    private Long id;

    @NotBlank(message = "客户姓名不能为空")
    @Schema(description = "客户姓名")
    private String customerName;

    @NotBlank(message = "客户电话不能为空")
    @Schema(description = "客户电话")
    private String customerPhone;

    @NotBlank(message = "配送地址不能为空")
    @Schema(description = "配送地址")
    private String deliveryAddress;

    @NotBlank(message = "配送区域不能为空")
    @Schema(description = "配送区域")
    private String deliveryArea;

    @Schema(description = "配送地址经度（地图选点，可为空）")
    private java.math.BigDecimal longitude;

    @Schema(description = "配送地址纬度（地图选点，可为空）")
    private java.math.BigDecimal latitude;

    @NotNull(message = "期望配送时间不能为空")
    @Schema(description = "期望配送时间")
    private LocalDateTime expectedDeliveryTime;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    @Schema(description = "订单明细")
    private List<OrderItemDTO> orderItems;

    @Data
    @Schema(description = "订单明细")
    public static class OrderItemDTO {
        @NotNull(message = "商品ID不能为空")
        @Schema(description = "商品ID")
        private Long dishId;

        @NotNull(message = "数量不能为空")
        @Schema(description = "数量")
        private Integer quantity;
    }
}
