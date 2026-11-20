package com.prefab.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 */
@Data
@Schema(description = "订单响应")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "客户电话")
    private String customerPhone;

    @Schema(description = "配送地址")
    private String deliveryAddress;

    @Schema(description = "配送区域")
    private String deliveryArea;

    @Schema(description = "配送地址经度（地图选点）")
    private BigDecimal longitude;

    @Schema(description = "配送地址纬度（地图选点）")
    private BigDecimal latitude;

    @Schema(description = "期望配送时间")
    private LocalDateTime expectedDeliveryTime;

    @Schema(description = "实际配送时间")
    private LocalDateTime actualDeliveryTime;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单明细")
    private List<OrderItemVO> orderItems;

    @Data
    @Schema(description = "订单明细")
    public static class OrderItemVO {
        @Schema(description = "明细ID")
        private Long id;
        @Schema(description = "商品ID")
        private Long dishId;
        @Schema(description = "商品名称")
        private String dishName;
        @Schema(description = "商品编码")
        private String dishCode;
        @Schema(description = "数量")
        private Integer quantity;
        @Schema(description = "单价")
        private BigDecimal unitPrice;
        @Schema(description = "小计")
        private BigDecimal subtotal;
    }
}
