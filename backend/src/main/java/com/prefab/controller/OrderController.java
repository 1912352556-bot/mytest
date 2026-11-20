package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.entity.Order;
import com.prefab.mapper.DishMapper;
import com.prefab.mapper.OrderMapper;
import com.prefab.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderMapper orderMapper;
    private final DishMapper dishMapper;
    private final OrderService orderService;

    @Operation(summary = "获取订单列表")
    @GetMapping("/list")
    public Result<PageResult<Order>> listOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer orderStatus) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), Order::getOrderNo, orderNo)
               .like(StringUtils.hasText(customerName), Order::getCustomerName, customerName)
               .eq(orderStatus != null, Order::getOrderStatus, orderStatus)
               .orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<com.prefab.vo.OrderVO> getOrderDetail(@PathVariable Long id) {
        try {
            com.prefab.vo.OrderVO orderVO = orderService.getOrderDetail(id);
            return Result.success(orderVO);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "新增订单")
    @PostMapping
    public Result<Object> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            // 转换为 DTO
            com.prefab.dto.OrderDTO orderDTO = new com.prefab.dto.OrderDTO();
            orderDTO.setCustomerName(request.getCustomerName());
            orderDTO.setCustomerPhone(request.getCustomerPhone());
            orderDTO.setDeliveryAddress(request.getDeliveryAddress());
            orderDTO.setDeliveryArea(request.getDeliveryArea());
            orderDTO.setExpectedDeliveryTime(request.getExpectedDeliveryTime());
            orderDTO.setRemark(request.getRemark());
            
            // 转换订单明细
            java.util.List<com.prefab.dto.OrderDTO.OrderItemDTO> itemDTOs = new java.util.ArrayList<>();
            if (request.getOrderItems() != null) {
                for (OrderItemRequest item : request.getOrderItems()) {
                    com.prefab.dto.OrderDTO.OrderItemDTO itemDTO = new com.prefab.dto.OrderDTO.OrderItemDTO();
                    itemDTO.setDishId(Long.parseLong(item.getDishId()));
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTOs.add(itemDTO);
                }
            }
            orderDTO.setOrderItems(itemDTOs);
            
            // 调用 Service 创建订单（会自动扣减库存）
            Long orderId = orderService.createOrder(orderDTO, 1L); // TODO: 从登录上下文获取 creatorId
            
            return Result.success(java.util.Map.of("orderId", orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("创建订单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新订单")
    @PutMapping
    public Result<Void> updateOrder(@RequestBody Order order) {
        orderMapper.updateById(order);
        return Result.success();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/status")
    public Result<Void> updateOrderStatus(@RequestBody UpdateStatusRequest request) {
        Order order = orderMapper.selectById(request.getId());
        if (order != null) {
            order.setOrderStatus(request.getStatus());
            orderMapper.updateById(order);
        }
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody(required = false) CancelRequest request) {
        try {
            String reason = request != null && request.getReason() != null ? request.getReason() : "用户取消";
            orderService.cancelOrder(id, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "批量导入订单")
    @PostMapping("/import")
    public Result<Object> importOrders() {
        return Result.success(new ImportResult(0, 0));
    }

    @Operation(summary = "导出订单")
    @GetMapping("/export")
    public Result<Void> exportOrders() {
        return Result.success();
    }

    @Operation(summary = "获取超时未调度订单")
    @GetMapping("/timeout")
    public Result<List<Order>> getTimeoutOrders() {
        return Result.success(new ArrayList<>());
    }

    @Operation(summary = "获取待调度订单列表")
    @GetMapping("/pending-schedule")
    public Result<List<Order>> getPendingScheduleOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderStatus, 0);
        List<Order> orders = orderMapper.selectList(wrapper);
        return Result.success(orders);
    }

    // 内部类
    @lombok.Data
    static class UpdateStatusRequest {
        private Long id;
        private Integer status;
    }

    @lombok.Data
    static class CancelRequest {
        private String reason;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class ImportResult {
        private int success;
        private int fail;
    }

    // 创建订单请求类
    @lombok.Data
    static class CreateOrderRequest {
        private String customerName;
        private String customerPhone;
        private String deliveryAddress;
        private String deliveryArea;
        private java.time.LocalDateTime expectedDeliveryTime;
        private String remark;
        private java.util.List<OrderItemRequest> orderItems;
    }

    @lombok.Data
    static class OrderItemRequest {
        private String dishId;
        private Integer quantity;
    }
}
