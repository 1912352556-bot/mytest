package com.prefab.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prefab.common.PageResult;
import com.prefab.common.ResultCode;
import com.prefab.dto.OrderDTO;
import com.prefab.dto.OrderQueryDTO;
import com.prefab.entity.Dish;
import com.prefab.entity.Inventory;
import com.prefab.entity.InventoryRecord;
import com.prefab.entity.Order;
import com.prefab.entity.OrderItem;
import com.prefab.entity.User;
import com.prefab.exception.BusinessException;
import com.prefab.mapper.DishMapper;
import com.prefab.mapper.InventoryMapper;
import com.prefab.mapper.InventoryRecordMapper;
import com.prefab.mapper.OrderItemMapper;
import com.prefab.mapper.OrderMapper;
import com.prefab.mapper.UserMapper;
import com.prefab.service.OrderService;
import com.prefab.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final UserMapper userMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    @Override
    public PageResult<OrderVO> pageOrders(OrderQueryDTO queryDTO) {
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getOrderNo()), Order::getOrderNo, queryDTO.getOrderNo())
               .like(StringUtils.hasText(queryDTO.getCustomerName()), Order::getCustomerName, queryDTO.getCustomerName())
               .eq(queryDTO.getOrderStatus() != null, Order::getOrderStatus, queryDTO.getOrderStatus())
               .eq(StringUtils.hasText(queryDTO.getDeliveryArea()), Order::getDeliveryArea, queryDTO.getDeliveryArea())
               .orderByDesc(Order::getCreateTime);
        
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            wrapper.ge(Order::getCreateTime, LocalDateTime.parse(queryDTO.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            wrapper.le(Order::getCreateTime, LocalDateTime.parse(queryDTO.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        Page<Order> result = this.page(page, wrapper);
        
        List<OrderVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        OrderVO orderVO = convertToVO(order);
        
        // 查询订单明细
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        
        List<OrderVO.OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
            BeanUtil.copyProperties(item, itemVO);
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish != null) {
                itemVO.setDishName(dish.getDishName());
                itemVO.setDishCode(dish.getDishCode());
            }
            return itemVO;
        }).collect(Collectors.toList());
        
        orderVO.setOrderItems(itemVOs);
        return orderVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderDTO orderDTO, Long creatorId) {
        // 生成订单编号
        String orderNo = "ORD" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        
        // 计算订单总金额，同时检查库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            if (dish == null) {
                throw new BusinessException(ResultCode.DISH_NOT_FOUND);
            }
            
            // 查询库存表获取实时库存
            LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(Inventory::getDishId, dish.getId());
            Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
            
            if (inventory == null) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH.getCode(), 
                    "商品【" + dish.getDishName() + "】库存记录不存在");
            }
            
            // 检查库存是否足够
            Integer currentStock = inventory.getCurrentStock() != null ? inventory.getCurrentStock() : 0;
            if (currentStock < itemDTO.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH.getCode(), 
                    "商品【" + dish.getDishName() + "】库存不足，当前库存：" + currentStock);
            }
            
            BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            
            OrderItem item = new OrderItem();
            item.setDishId(itemDTO.getDishId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(dish.getPrice());
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }
        
        // 创建订单
        Order order = new Order();
        BeanUtil.copyProperties(orderDTO, order);
        order.setOrderNo(orderNo);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(0); // 待调度
        order.setCreatorId(creatorId);
        
        this.save(order);
        
        // 保存订单明细并扣减库存
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            
            // 扣减库存：只更新 t_inventory 表
            LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(Inventory::getDishId, item.getDishId());
            Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
            
            if (inventory != null) {
                int beforeStock = inventory.getCurrentStock();
                inventory.setCurrentStock(inventory.getCurrentStock() - item.getQuantity());
                inventory.setLastOutTime(LocalDateTime.now());
                inventoryMapper.updateById(inventory);
                
                // 记录库存变更
                InventoryRecord record = new InventoryRecord();
                record.setInventoryId(inventory.getId());
                record.setChangeType(2); // 2-订单出库
                record.setQuantity(item.getQuantity());
                record.setBeforeStock(beforeStock);
                record.setAfterStock(inventory.getCurrentStock());
                record.setOrderId(order.getId());
                record.setRemark("订单号：" + order.getOrderNo() + " 生成时扣减库存");
                record.setOperatorId(creatorId);
                inventoryRecordMapper.insert(record);
            }
        }
        
        return order.getId();
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer status) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        order.setOrderStatus(status);
        if (status == 3) { // 已完成
            order.setActualDeliveryTime(LocalDateTime.now());
        }
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        if (order.getOrderStatus() >= 2) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL);
        }
        
        // 查询订单明细，补回库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        
        for (OrderItem item : items) {
            // 补回库存：只更新 t_inventory 表
            LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(Inventory::getDishId, item.getDishId());
            Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
            
            if (inventory != null) {
                int beforeStock = inventory.getCurrentStock();
                inventory.setCurrentStock(inventory.getCurrentStock() + item.getQuantity());
                inventory.setLastInTime(LocalDateTime.now());
                inventoryMapper.updateById(inventory);
                
                // 记录库存变更
                InventoryRecord record = new InventoryRecord();
                record.setInventoryId(inventory.getId());
                record.setChangeType(0); // 0-入库
                record.setQuantity(item.getQuantity());
                record.setBeforeStock(beforeStock);
                record.setAfterStock(inventory.getCurrentStock());
                record.setOrderId(order.getId());
                record.setRemark("订单号：" + order.getOrderNo() + " 取消时补回库存");
                record.setOperatorId(order.getCreatorId());
                inventoryRecordMapper.insert(record);
            }
        }
        
        order.setOrderStatus(4); // 已取消
        order.setRemark(reason);
        this.updateById(order);
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        
        // 查询创建人姓名
        if (order.getCreatorId() != null) {
            User user = userMapper.selectById(order.getCreatorId());
            if (user != null) {
                vo.setCreatorName(user.getRealname());
            }
        }
        
        return vo;
    }
}
