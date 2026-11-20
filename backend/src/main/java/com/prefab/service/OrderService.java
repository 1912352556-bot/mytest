package com.prefab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.prefab.common.PageResult;
import com.prefab.dto.OrderDTO;
import com.prefab.dto.OrderQueryDTO;
import com.prefab.entity.Order;
import com.prefab.vo.OrderVO;

/**
 * 订单Service接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 分页查询订单
     */
    PageResult<OrderVO> pageOrders(OrderQueryDTO queryDTO);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long orderId);

    /**
     * 创建订单
     */
    Long createOrder(OrderDTO orderDTO, Long creatorId);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, Integer status);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, String reason);
}
