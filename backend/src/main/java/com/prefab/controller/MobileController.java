package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.dto.OrderDTO;
import com.prefab.entity.*;
import com.prefab.mapper.*;
import com.prefab.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 移动端控制器
 *
 * <p>面向两类移动端用户：</p>
 * <ul>
 *     <li>客户（role=3）：浏览商品、下单、查看/取消自己的订单</li>
 *     <li>配送员（role=2）：查看并处理分配给自己的配送任务</li>
 * </ul>
 * <p>所有接口均依赖 JWT 拦截器写入 {@code userId} / {@code role} 请求属性，不会从请求参数里取用户身份。</p>
 */
@Tag(name = "移动端")
@RestController
@RequestMapping("/mobile")
@RequiredArgsConstructor
public class MobileController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 角色：客户 */
    private static final int ROLE_CUSTOMER = 3;
    /** 角色：配送员 */
    private static final int ROLE_DELIVERY = 2;

    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final DishCategoryMapper dishCategoryMapper;
    private final InventoryMapper inventoryMapper;
    private final OrderMapper orderMapper;
    private final DeliveryTaskMapper deliveryTaskMapper;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final ScheduleTaskMapper scheduleTaskMapper;
    private final OrderService orderService;
    private final com.prefab.service.AutoDispatchService autoDispatchService;

    // ==================== 通用 ====================

    @Operation(summary = "获取当前登录用户资料")
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(HttpServletRequest request) {
        Long userId = currentUserId(request);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realname", user.getRealname());
        data.put("phone", user.getPhone());
        data.put("role", user.getRole());
        data.put("roleName", roleName(user.getRole()));
        data.put("avatar", user.getAvatar());
        if (Integer.valueOf(ROLE_DELIVERY).equals(user.getRole())) {
            DeliveryPerson person = findPerson(userId, false);
            if (person != null) {
                data.put("person", person);
            }
        }
        return Result.success(data);
    }

    // ==================== 客户：商品与下单 ====================

    @Operation(summary = "客户首页商品数据")
    @GetMapping("/customer/home")
    public Result<Map<String, Object>> customerHome(HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_CUSTOMER);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        List<DishCategory> categories = dishCategoryMapper.selectList(
                new LambdaQueryWrapper<DishCategory>()
                        .eq(DishCategory::getStatus, 1)
                        .orderByAsc(DishCategory::getSortOrder));

        List<Dish> dishes = dishMapper.selectList(
                new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getStatus, 1)
                        .orderByDesc(Dish::getCreateTime));
        fillStock(dishes);

        Map<Long, String> categoryNames = categories.stream()
                .collect(Collectors.toMap(DishCategory::getId, DishCategory::getCategoryName, (a, b) -> a));

        List<Map<String, Object>> dishViews = new ArrayList<>();
        for (Dish dish : dishes) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", dish.getId());
            view.put("dishName", dish.getDishName());
            view.put("dishCode", dish.getDishCode());
            view.put("categoryId", dish.getCategoryId());
            view.put("categoryName", categoryNames.get(dish.getCategoryId()));
            view.put("price", dish.getPrice());
            view.put("unit", dish.getUnit());
            view.put("spec", dish.getSpec());
            view.put("description", dish.getDescription());
            view.put("imageUrl", dish.getImageUrl());
            view.put("stock", dish.getStock());
            dishViews.add(view);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", categories);
        data.put("dishes", dishViews);
        return Result.success(data);
    }

    @Operation(summary = "客户下单")
    @PostMapping("/customer/orders")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> createOrder(@RequestBody MobileOrderRequest body, HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_CUSTOMER);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        Long userId = currentUserId(request);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (body.getItems() == null || body.getItems().isEmpty()) {
            return Result.fail("请至少选择一件商品");
        }
        if (!StringUtils.hasText(body.getDeliveryAddress())) {
            return Result.fail("请填写配送地址");
        }

        String customerName = StringUtils.hasText(body.getCustomerName())
                ? body.getCustomerName() : user.getRealname();
        String customerPhone = StringUtils.hasText(body.getCustomerPhone())
                ? body.getCustomerPhone() : user.getPhone();
        if (!StringUtils.hasText(customerName) || !StringUtils.hasText(customerPhone)) {
            return Result.fail("请先完善联系人姓名和手机号");
        }

        OrderDTO dto = new OrderDTO();
        dto.setCustomerName(customerName);
        dto.setCustomerPhone(customerPhone);
        dto.setDeliveryAddress(body.getDeliveryAddress());
        dto.setDeliveryArea(StringUtils.hasText(body.getDeliveryArea())
                ? body.getDeliveryArea() : "默认区域");
        dto.setExpectedDeliveryTime(parseTime(body.getExpectedDeliveryTime()));
        dto.setRemark(body.getRemark());
        dto.setLongitude(body.getLongitude());
        dto.setLatitude(body.getLatitude());

        List<OrderDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        for (MobileOrderItem item : body.getItems()) {
            if (item.getDishId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return Result.fail("商品或数量不正确");
            }
            OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
            itemDTO.setDishId(item.getDishId());
            itemDTO.setQuantity(item.getQuantity());
            itemDTOs.add(itemDTO);
        }
        dto.setOrderItems(itemDTOs);

        Long orderId = orderService.createOrder(dto, userId);

        /* 下单后立即自动派单：有可用配送员则直接进入「已调度」，否则留在「待调度」由调度员处理 */
        com.prefab.service.AutoDispatchService.DispatchResult dispatch =
                autoDispatchService.autoAssignOnCreate(orderId);
        /* 重新读取订单以拿到派单后的最新状态 */
        Order order = orderMapper.selectById(orderId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("orderNo", order == null ? null : order.getOrderNo());
        data.put("totalAmount", order == null ? null : order.getTotalAmount());
        data.put("orderStatus", order == null ? null : order.getOrderStatus());
        data.put("autoAssigned", dispatch.isAssigned());
        data.put("dispatchMessage", dispatch.getMessage());
        data.put("deliveryPersonName", dispatch.getDeliveryPersonName());
        return Result.success(data);
    }

    @Operation(summary = "客户订单列表（仅自己的订单）")
    @GetMapping("/customer/orders")
    public Result<PageResult<Order>> myOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Integer orderStatus,
                                              HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_CUSTOMER);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        Long userId = currentUserId(request);
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCreatorId, userId)
                .eq(orderStatus != null, Order::getOrderStatus, orderStatus)
                .orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "客户订单详情（仅自己的订单）")
    @GetMapping("/customer/orders/{id}")
    public Result<com.prefab.vo.OrderVO> myOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_CUSTOMER);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        Long userId = currentUserId(request);
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!java.util.Objects.equals(order.getCreatorId(), userId)) {
            return Result.fail("无权查看该订单");
        }
        return Result.success(orderService.getOrderDetail(id));
    }

    @Operation(summary = "客户取消订单（仅自己的订单）")
    @PutMapping("/customer/orders/{id}/cancel")
    public Result<Void> cancelMyOrder(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, String> body,
                                      HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_CUSTOMER);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        Long userId = currentUserId(request);
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!java.util.Objects.equals(order.getCreatorId(), userId)) {
            return Result.fail("无权操作该订单");
        }
        String reason = body != null && StringUtils.hasText(body.get("reason"))
                ? body.get("reason") : "客户取消";
        try {
            orderService.cancelOrder(id, reason);
            /* 同步释放已派出的配送任务与调度任务 */
            autoDispatchService.releaseOrderDispatch(id, "订单已取消：" + reason, 4);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // ==================== 配送员：我的配送任务 ====================

    @Operation(summary = "配送员任务列表（仅自己的任务）")
    @GetMapping("/courier/tasks")
    public Result<PageResult<DeliveryTask>> myTasks(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) Integer deliveryStatus,
                                                    HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        Long userId = currentUserId(request);
        DeliveryPerson person = findPerson(userId, true);
        if (person == null) {
            return Result.fail("当前账号未关联配送员档案，请联系调度员");
        }
        Page<DeliveryTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getDeliveryPersonId, person.getId())
                .eq(deliveryStatus != null, DeliveryTask::getDeliveryStatus, deliveryStatus)
                .orderByAsc(DeliveryTask::getDeliveryStatus)
                .orderByDesc(DeliveryTask::getCreateTime);
        Page<DeliveryTask> result = deliveryTaskMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillTaskOrderInfo);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "配送员任务详情（仅自己的任务）")
    @GetMapping("/courier/tasks/{id}")
    public Result<DeliveryTask> myTaskDetail(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        Long userId = currentUserId(request);
        DeliveryPerson person = findPerson(userId, true);
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        if (task == null) {
            return Result.fail("配送任务不存在");
        }
        if (person == null || !java.util.Objects.equals(task.getDeliveryPersonId(), person.getId())) {
            return Result.fail("无权查看该配送任务");
        }
        fillTaskOrderInfo(task);
        return Result.success(task);
    }

    @Operation(summary = "开始配送")
    @PutMapping("/courier/tasks/{id}/start")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> startDelivery(@PathVariable Long id, HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        DeliveryPerson person = findPerson(currentUserId(request), true);
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        Result<Void> ownerDenied = checkTaskOwner(task, person);
        if (ownerDenied != null) {
            return ownerDenied;
        }
        if (!Integer.valueOf(0).equals(task.getDeliveryStatus())) {
            return Result.fail("当前任务状态不允许开始配送");
        }

        task.setDeliveryStatus(1);
        task.setPickupTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);

        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setOrderStatus(2);
            orderMapper.updateById(order);
        }
        ScheduleTask scheduleTask = scheduleTaskMapper.selectById(task.getScheduleTaskId());
        if (scheduleTask != null) {
            scheduleTask.setTaskStatus(2);
            scheduleTaskMapper.updateById(scheduleTask);
        }
        person.setStatus(1);
        deliveryPersonMapper.updateById(person);
        return Result.success();
    }

    @Operation(summary = "完成配送")
    @PutMapping("/courier/tasks/{id}/complete")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> completeDelivery(@PathVariable Long id,
                                         @RequestBody(required = false) Map<String, String> body,
                                         HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        DeliveryPerson person = findPerson(currentUserId(request), true);
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        Result<Void> deniedOwner = checkTaskOwner(task, person);
        if (deniedOwner != null) {
            return deniedOwner;
        }
        if (!Integer.valueOf(1).equals(task.getDeliveryStatus())) {
            return Result.fail("请先开始配送再确认送达");
        }

        LocalDateTime now = LocalDateTime.now();
        task.setDeliveryStatus(2);
        task.setDeliveryTime(now);
        if (body != null && StringUtils.hasText(body.get("remark"))) {
            task.setRemark(body.get("remark"));
        }
        deliveryTaskMapper.updateById(task);

        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setOrderStatus(3);
            order.setActualDeliveryTime(now);
            orderMapper.updateById(order);
        }
        ScheduleTask scheduleTask = scheduleTaskMapper.selectById(task.getScheduleTaskId());
        if (scheduleTask != null) {
            scheduleTask.setTaskStatus(3);
            scheduleTaskMapper.updateById(scheduleTask);
        }

        int currentCount = person.getCurrentTaskCount() == null ? 0 : person.getCurrentTaskCount();
        person.setCurrentTaskCount(Math.max(0, currentCount - 1));
        person.setTotalDeliveryCount((person.getTotalDeliveryCount() == null ? 0 : person.getTotalDeliveryCount()) + 1);
        if (person.getCurrentTaskCount() == 0) {
            person.setStatus(0);
        }
        deliveryPersonMapper.updateById(person);
        return Result.success();
    }

    @Operation(summary = "异常上报")
    @PutMapping("/courier/tasks/{id}/exception")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> reportException(@PathVariable Long id,
                                        @RequestBody(required = false) Map<String, String> body,
                                        HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        DeliveryPerson person = findPerson(currentUserId(request), true);
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        Result<Void> deniedOwner = checkTaskOwner(task, person);
        if (deniedOwner != null) {
            return deniedOwner;
        }
        if (Integer.valueOf(2).equals(task.getDeliveryStatus())) {
            return Result.fail("已送达的任务不能上报异常");
        }
        String reason = body != null && StringUtils.hasText(body.get("remark"))
                ? body.get("remark") : "配送异常";

        /* 标记异常并把订单退回待调度、归还配送员任务额度，便于重新派单 */
        autoDispatchService.releaseOrderDispatch(task.getOrderId(), "配送异常：" + reason, 0);
        return Result.success();
    }

    @Operation(summary = "配送员工作台统计")
    @GetMapping("/courier/summary")
    public Result<Map<String, Object>> courierSummary(HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        DeliveryPerson person = findPerson(currentUserId(request), true);
        if (person == null) {
            return Result.fail("当前账号未关联配送员档案，请联系调度员");
        }
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long pending = deliveryTaskMapper.selectCount(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getDeliveryPersonId, person.getId())
                .eq(DeliveryTask::getDeliveryStatus, 0));
        long delivering = deliveryTaskMapper.selectCount(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getDeliveryPersonId, person.getId())
                .eq(DeliveryTask::getDeliveryStatus, 1));
        long completedToday = deliveryTaskMapper.selectCount(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getDeliveryPersonId, person.getId())
                .eq(DeliveryTask::getDeliveryStatus, 2)
                .ge(DeliveryTask::getDeliveryTime, todayStart));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("personId", person.getId());
        data.put("personName", person.getPersonName());
        data.put("phone", person.getPhone());
        data.put("vehicleNo", person.getVehicleNo());
        data.put("status", person.getStatus());
        data.put("currentTaskCount", person.getCurrentTaskCount());
        data.put("totalDeliveryCount", person.getTotalDeliveryCount());
        data.put("pendingCount", pending);
        data.put("deliveringCount", delivering);
        data.put("completedTodayCount", completedToday);
        return Result.success(data);
    }

    @Operation(summary = "配送员上下班状态：0-空闲 1-配送中 2-休息")
    @PutMapping("/courier/status/{status}")
    public Result<Void> updateCourierStatus(@PathVariable Integer status, HttpServletRequest request) {
        Result<Void> denied = requireRole(request, ROLE_DELIVERY);
        if (denied != null) {
            return Result.fail(denied.getMsg());
        }
        if (status == null || status < 0 || status > 2) {
            return Result.fail("状态值不合法");
        }
        DeliveryPerson person = findPerson(currentUserId(request), true);
        if (person == null) {
            return Result.fail("当前账号未关联配送员档案");
        }
        person.setStatus(status);
        deliveryPersonMapper.updateById(person);
        return Result.success();
    }

    // ==================== 内部方法 ====================

    private Long currentUserId(HttpServletRequest request) {
        Object value = request.getAttribute("userId");
        if (value == null) {
            throw new IllegalStateException("未获取到登录用户信息");
        }
        return Long.valueOf(value.toString());
    }

    /**
     * 校验当前账号角色：只有传入的角色可以访问对应接口
     *
     * @return null 表示校验通过，否则返回拒绝结果
     */
    private Result<Void> requireRole(HttpServletRequest request, int... expectedRoles) {
        Object value = request.getAttribute("role");
        int role = value == null ? -1 : Integer.parseInt(value.toString());
        for (int expected : expectedRoles) {
            if (role == expected) {
                return null;
            }
        }
        return Result.fail("当前账号无权访问该功能");
    }

    /** 客户手机号等资料校验用：确保角色一致（预留，必要时启用） */
    @SuppressWarnings("unused")
    private boolean isCustomer(User user) {
        return user != null && Integer.valueOf(ROLE_CUSTOMER).equals(user.getRole());
    }

    private String roleName(Integer role) {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case 0:
                return "管理员";
            case 1:
                return "调度员";
            case 2:
                return "配送员";
            case 3:
                return "客户";
            default:
                return "未知";
        }
    }

    /**
     * 查找配送员档案；create 为 true 且不存在时自动建档（同一个用户只会建一条）
     */
    private DeliveryPerson findPerson(Long userId, boolean create) {
        List<DeliveryPerson> list = deliveryPersonMapper.selectList(
                new LambdaQueryWrapper<DeliveryPerson>()
                        .eq(DeliveryPerson::getUserId, userId)
                        .last("limit 1"));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        if (!create) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        DeliveryPerson person = new DeliveryPerson();
        person.setUserId(userId);
        person.setPersonName(user.getRealname());
        person.setPhone(user.getPhone());
        person.setStatus(0);
        person.setCurrentTaskCount(0);
        person.setTotalDeliveryCount(0);
        deliveryPersonMapper.insert(person);
        return person;
    }

    private Result<Void> checkTaskOwner(DeliveryTask task, DeliveryPerson person) {
        if (task == null) {
            return Result.fail("配送任务不存在");
        }
        if (person == null || !java.util.Objects.equals(task.getDeliveryPersonId(), person.getId())) {
            return Result.fail("无权操作该配送任务");
        }
        return null;
    }

    private void fillTaskOrderInfo(DeliveryTask task) {
        if (task.getOrderId() == null) {
            return;
        }
        Order order = orderMapper.selectById(task.getOrderId());
        if (order == null) {
            return;
        }
        task.setOrderNo(order.getOrderNo());
        task.setCustomerName(order.getCustomerName());
        task.setCustomerPhone(order.getCustomerPhone());
        task.setExpectedDeliveryTime(order.getExpectedDeliveryTime());
        task.setOrderStatus(order.getOrderStatus());
        task.setTotalAmount(order.getTotalAmount());
        task.setLongitude(order.getLongitude());
        task.setLatitude(order.getLatitude());
        if (!StringUtils.hasText(task.getDeliveryAddress())) {
            task.setDeliveryAddress(order.getDeliveryAddress());
        }
    }

    private void fillStock(List<Dish> dishes) {
        for (Dish dish : dishes) {
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getDishId, dish.getId())
                    .last("limit 1"));
            dish.setStock(inventory == null || inventory.getCurrentStock() == null ? 0 : inventory.getCurrentStock());
        }
    }

    private LocalDateTime parseTime(String text) {
        if (!StringUtils.hasText(text)) {
            return LocalDateTime.now().plusHours(1).withSecond(0).withNano(0);
        }
        String value = text.trim().replace('T', ' ');
        if (value.length() == 16) {
            value = value + ":00";
        }
        if (value.length() > 19) {
            value = value.substring(0, 19);
        }
        try {
            return LocalDateTime.parse(value, TIME_FORMATTER);
        } catch (Exception ignored) {
            return LocalDateTime.now().plusHours(1).withSecond(0).withNano(0);
        }
    }

    // ==================== 请求体 ====================

    @lombok.Data
    static class MobileOrderRequest {
        private String customerName;
        private String customerPhone;
        private String deliveryAddress;
        private String deliveryArea;
        private String expectedDeliveryTime;
        private String remark;
        /** 地图选点得到的经度，可为空 */
        private java.math.BigDecimal longitude;
        /** 地图选点得到的纬度，可为空 */
        private java.math.BigDecimal latitude;
        private List<MobileOrderItem> items;
    }

    @lombok.Data
    static class MobileOrderItem {
        private Long dishId;
        private Integer quantity;
    }
}
