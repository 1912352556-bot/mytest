package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prefab.common.Result;
import com.prefab.entity.*;
import com.prefab.mapper.DeliveryTaskMapper;
import com.prefab.mapper.DishMapper;
import com.prefab.mapper.InventoryMapper;
import com.prefab.mapper.InventoryRecordMapper;
import com.prefab.mapper.OrderItemMapper;
import com.prefab.mapper.OrderMapper;
import com.prefab.mapper.ScheduleTaskMapper;
import com.prefab.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析控制器
 */
@Tag(name = "统计分析")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final ScheduleTaskMapper scheduleTaskMapper;
    private final DeliveryTaskMapper deliveryTaskMapper;
    private final DishMapper dishMapper;

    @Operation(summary = "获取首页统计概览")
    @GetMapping("/overview")
    public Result<DashboardVO> getDashboard() {
        DashboardVO vo = new DashboardVO();

        // 获取今日开始和结束时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 查询今日订单总数
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.between(Order::getCreateTime, todayStart, todayEnd);
        vo.setTodayOrderCount(orderMapper.selectCount(todayOrderWrapper).intValue());

        // 查询今日已完成订单数
        LambdaQueryWrapper<Order> todayCompletedWrapper = new LambdaQueryWrapper<>();
        todayCompletedWrapper.between(Order::getCreateTime, todayStart, todayEnd)
                .eq(Order::getOrderStatus, 3);
        vo.setTodayCompletedCount(orderMapper.selectCount(todayCompletedWrapper).intValue());

        // 查询待调度订单数（状态为0）
        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getOrderStatus, 0);
        vo.setPendingScheduleCount(orderMapper.selectCount(pendingWrapper).intValue());

        // 查询配送中订单数（状态为2）
        LambdaQueryWrapper<Order> deliveringWrapper = new LambdaQueryWrapper<>();
        deliveringWrapper.eq(Order::getOrderStatus, 2);
        vo.setDeliveringCount(orderMapper.selectCount(deliveringWrapper).intValue());

        // 查询库存预警数量（当前库存低于预警阈值）
        // 使用自定义SQL条件比较两个字段
        LambdaQueryWrapper<Inventory> warningWrapper = new LambdaQueryWrapper<>();
        warningWrapper.apply("current_stock < warning_threshold");
        vo.setLowStockCount(inventoryMapper.selectCount(warningWrapper).intValue());

        // 查询今日订单总金额
        LambdaQueryWrapper<Order> amountWrapper = new LambdaQueryWrapper<>();
        amountWrapper.between(Order::getCreateTime, todayStart, todayEnd);
        List<Order> todayOrders = orderMapper.selectList(amountWrapper);
        BigDecimal totalAmount = todayOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalOrderAmount(totalAmount);

        return Result.success(vo);
    }

    @Operation(summary = "获取订单统计")
    @GetMapping("/order")
    public Result<List<Map<String, Object>>> getOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 默认查询最近7天
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();

        // 查询日期范围内的订单数据
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            // 查询当日订单总数
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Order::getCreateTime, dayStart, dayEnd);
            long orderCount = orderMapper.selectCount(wrapper);

            // 查询当日已完成订单数
            LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.between(Order::getCreateTime, dayStart, dayEnd)
                    .eq(Order::getOrderStatus, 3);
            long completedCount = orderMapper.selectCount(completedWrapper);

            // 查询当日已取消订单数
            LambdaQueryWrapper<Order> cancelledWrapper = new LambdaQueryWrapper<>();
            cancelledWrapper.between(Order::getCreateTime, dayStart, dayEnd)
                    .eq(Order::getOrderStatus, 4);
            long cancelledCount = orderMapper.selectCount(cancelledWrapper);

            // 查询当日订单总金额
            List<Order> orders = orderMapper.selectList(wrapper);
            BigDecimal totalAmount = orders.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("orderCount", orderCount);
            item.put("completedCount", completedCount);
            item.put("cancelledCount", cancelledCount);
            item.put("totalAmount", totalAmount);
            list.add(item);
        }

        return Result.success(list);
    }

    @Operation(summary = "获取调度统计")
    @GetMapping("/schedule")
    public Result<List<Map<String, Object>>> getScheduleStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 默认查询最近7天
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();

        // 查询日期范围内的调度任务数据
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            // 查询当日调度任务总数
            LambdaQueryWrapper<ScheduleTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(ScheduleTask::getCreateTime, dayStart, dayEnd);
            long totalTasks = scheduleTaskMapper.selectCount(wrapper);

            // 查询当日已完成调度任务数
            LambdaQueryWrapper<ScheduleTask> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.between(ScheduleTask::getCreateTime, dayStart, dayEnd)
                    .eq(ScheduleTask::getTaskStatus, 3);
            long completedTasks = scheduleTaskMapper.selectCount(completedWrapper);

            // 计算完成率
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("totalTasks", totalTasks);
            item.put("completedTasks", completedTasks);
            item.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            list.add(item);
        }

        return Result.success(list);
    }

    @Operation(summary = "获取库存周转统计")
    @GetMapping("/inventory-turnover")
    public Result<List<Map<String, Object>>> getInventoryTurnoverStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 设置默认查询时间范围为最近30天
        LocalDateTime start = (startDate != null)
                ? LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN)
                : LocalDateTime.now().minusDays(30);
        LocalDateTime end = (endDate != null)
                ? LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX)
                : LocalDateTime.now();

        // 查询所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(null);

        for (Inventory inventory : inventories) {
            Long inventoryId = inventory.getId();
            Long dishId = inventory.getDishId();
            if (inventoryId == null) continue;

            // 查询该库存在日期范围内的入库记录 (change_type = 1 表示入库)
            LambdaQueryWrapper<InventoryRecord> inWrapper = new LambdaQueryWrapper<>();
            inWrapper.eq(InventoryRecord::getInventoryId, inventoryId)
                    .eq(InventoryRecord::getChangeType, 1)
                    .between(InventoryRecord::getCreateTime, start, end);
            List<InventoryRecord> inRecords = inventoryRecordMapper.selectList(inWrapper);
            int inQuantity = inRecords.stream().mapToInt(InventoryRecord::getQuantity).sum();

            // 查询该库存在日期范围内的出库记录 (change_type = 2 表示出库)
            LambdaQueryWrapper<InventoryRecord> outWrapper = new LambdaQueryWrapper<>();
            outWrapper.eq(InventoryRecord::getInventoryId, inventoryId)
                    .eq(InventoryRecord::getChangeType, 2)
                    .between(InventoryRecord::getCreateTime, start, end);
            List<InventoryRecord> outRecords = inventoryRecordMapper.selectList(outWrapper);
            int outQuantity = outRecords.stream().mapToInt(InventoryRecord::getQuantity).sum();

            // 计算周转率
            double turnoverRate = (inQuantity + inventory.getCurrentStock()) > 0
                    ? (double) outQuantity / (inQuantity + inventory.getCurrentStock()) * 100
                    : 0;

            // 查询商品名称
            String dishName = "未知商品";
            if (dishId != null) {
                Dish dish = dishMapper.selectById(dishId);
                if (dish != null && dish.getDishName() != null) {
                    dishName = dish.getDishName();
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("dishName", dishName);
            item.put("inQuantity", inQuantity);
            item.put("outQuantity", outQuantity);
            item.put("currentStock", inventory.getCurrentStock());
            item.put("turnoverRate", Math.round(turnoverRate * 100.0) / 100.0);
            list.add(item);
        }

        return Result.success(list);
    }

    @Operation(summary = "获取每日订单量统计")
    @GetMapping("/order/daily")
    public Result<List<Map<String, Object>>> getDailyOrderCount(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 默认查询最近7天
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();

        // 查询日期范围内的订单数据
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Order::getCreateTime, dayStart, dayEnd);
            long count = orderMapper.selectCount(wrapper);

            // 查询已完成订单数
            LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.between(Order::getCreateTime, dayStart, dayEnd)
                    .eq(Order::getOrderStatus, 3);
            long completedCount = orderMapper.selectCount(completedWrapper);

            // 查询当日订单总金额
            List<Order> orders = orderMapper.selectList(wrapper);
            BigDecimal amount = orders.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            item.put("completedCount", completedCount);
            item.put("amount", amount);
            list.add(item);
        }

        return Result.success(list);
    }

    @Operation(summary = "获取每月订单量统计")
    @GetMapping("/order/monthly")
    public Result<List<Map<String, Object>>> getMonthlyOrderCount(@RequestParam(required = false) Integer year) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 默认查询当前年份
        int queryYear = (year != null) ? year : LocalDate.now().getYear();

        // 查询每个月的订单数量
        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(queryYear, month, 1, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Order::getCreateTime, monthStart, monthEnd);
            long count = orderMapper.selectCount(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("month", month + "月");
            item.put("count", count);
            list.add(item);
        }

        return Result.success(list);
    }

    @Operation(summary = "获取订单状态分布")
    @GetMapping("/order/status-distribution")
    public Result<List<Map<String, Object>>> getOrderStatusDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] statusNames = {"待调度", "已调度", "配送中", "已完成", "已取消"};

        for (int i = 0; i < statusNames.length; i++) {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderStatus, i);
            long count = orderMapper.selectCount(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("status", i);
            item.put("statusName", statusNames[i]);
            item.put("count", count);
            list.add(item);
        }
        return Result.success(list);
    }

    @Operation(summary = "获取配送区域订单分布")
    @GetMapping("/order/area-distribution")
    public Result<List<Map<String, Object>>> getAreaOrderDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();

        // 查询所有订单并按区域分组统计
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Order::getDeliveryArea, Order::getTotalAmount);
        wrapper.isNotNull(Order::getDeliveryArea);
        List<Order> orders = orderMapper.selectList(wrapper);

        // 按区域分组统计
        Map<String, List<Order>> areaGroup = orders.stream()
                .filter(o -> o.getDeliveryArea() != null && !o.getDeliveryArea().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(Order::getDeliveryArea));

        for (Map.Entry<String, List<Order>> entry : areaGroup.entrySet()) {
            String area = entry.getKey();
            List<Order> areaOrders = entry.getValue();

            BigDecimal totalAmount = areaOrders.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("area", area);
            item.put("count", areaOrders.size());
            item.put("amount", totalAmount);
            list.add(item);
        }

        // 按订单数量降序排序
        list.sort((a, b) -> ((Long) b.get("count")).compareTo((Long) a.get("count")));

        return Result.success(list);
    }

    @Operation(summary = "获取热销商品排行")
    @GetMapping("/dish/hot-ranking")
    public Result<List<Map<String, Object>>> getHotDishRanking(@RequestParam(required = false) Integer limit) {
        // 默认返回前5名
        int topLimit = (limit != null && limit > 0) ? limit : 5;

        // 查询所有订单明细
        List<OrderItem> orderItems = orderItemMapper.selectList(null);

        // 按商品ID分组统计销量和金额
        Map<Long, List<OrderItem>> dishGroup = orderItems.stream()
                .filter(o -> o.getDishId() != null)
                .collect(Collectors.groupingBy(OrderItem::getDishId));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : dishGroup.entrySet()) {
            Long dishId = entry.getKey();
            List<OrderItem> items = entry.getValue();

            int totalQuantity = items.stream().mapToInt(OrderItem::getQuantity).sum();
            BigDecimal totalAmount = items.stream()
                    .map(OrderItem::getSubtotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 从商品表查询商品名称
            String dishName = "未知商品";
            Dish dish = dishMapper.selectById(dishId);
            if (dish != null && dish.getDishName() != null) {
                dishName = dish.getDishName();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("dishId", String.valueOf(dishId));
            item.put("dishName", dishName);
            item.put("quantity", totalQuantity);
            item.put("amount", totalAmount);
            list.add(item);
        }

        // 按销量降序排序并取前N名
        list.sort((a, b) -> ((Integer) b.get("quantity")).compareTo((Integer) a.get("quantity")));
        if (list.size() > topLimit) {
            list = list.subList(0, topLimit);
        }

        return Result.success(list);
    }

    @Operation(summary = "获取配送人员绩效统计")
    @GetMapping("/delivery/performance")
    public Result<List<Map<String, Object>>> getDeliveryPersonPerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 设置默认查询时间范围为最近30天
        LocalDateTime start = (startDate != null)
                ? LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN)
                : LocalDateTime.now().minusDays(30);
        LocalDateTime end = (endDate != null)
                ? LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX)
                : LocalDateTime.now();

        // 查询所有已完成的配送任务
        LambdaQueryWrapper<com.prefab.entity.DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.prefab.entity.DeliveryTask::getDeliveryStatus, 2)
                .between(com.prefab.entity.DeliveryTask::getCreateTime, start, end)
                .isNotNull(com.prefab.entity.DeliveryTask::getDeliveryPersonId);
        List<com.prefab.entity.DeliveryTask> tasks = deliveryTaskMapper.selectList(wrapper);

        // 按配送人员分组统计
        Map<Long, List<com.prefab.entity.DeliveryTask>> personGroup = tasks.stream()
                .filter(t -> t.getDeliveryPersonId() != null)
                .collect(Collectors.groupingBy(com.prefab.entity.DeliveryTask::getDeliveryPersonId));

        for (Map.Entry<Long, List<com.prefab.entity.DeliveryTask>> entry : personGroup.entrySet()) {
            Long personId = entry.getKey();
            List<com.prefab.entity.DeliveryTask> personTasks = entry.getValue();

            // 计算平均配送时间（分钟）
            long totalMinutes = 0;
            int validTasks = 0;
            for (com.prefab.entity.DeliveryTask task : personTasks) {
                if (task.getPickupTime() != null && task.getDeliveryTime() != null) {
                    long minutes = java.time.Duration.between(task.getPickupTime(), task.getDeliveryTime()).toMinutes();
                    if (minutes > 0) {
                        totalMinutes += minutes;
                        validTasks++;
                    }
                }
            }
            long avgTime = validTasks > 0 ? totalMinutes / validTasks : 0;

            // 获取配送人员姓名
            String personName = personTasks.stream()
                    .findFirst()
                    .map(com.prefab.entity.DeliveryTask::getDeliveryPersonName)
                    .orElse("未知人员");

            Map<String, Object> item = new HashMap<>();
            item.put("personId", String.valueOf(personId));
            item.put("personName", personName);
            item.put("deliveryCount", personTasks.size());
            item.put("avgTime", avgTime);
            list.add(item);
        }

        // 按配送数量降序排序
        list.sort((a, b) -> ((Integer) b.get("deliveryCount")).compareTo((Integer) a.get("deliveryCount")));

        return Result.success(list);
    }

    @Operation(summary = "获取库存统计")
    @GetMapping("/inventory")
    public Result<List<Map<String, Object>>> getInventoryStatistics() {
        List<Map<String, Object>> list = new ArrayList<>();

        // 查询所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(null);

        for (Inventory inventory : inventories) {
            Long inventoryId = inventory.getId();
            Long dishId = inventory.getDishId();
            if (inventoryId == null) continue;

            // 查询该库存的所有入库记录 (change_type = 1 表示入库)
            LambdaQueryWrapper<InventoryRecord> inWrapper = new LambdaQueryWrapper<>();
            inWrapper.eq(InventoryRecord::getInventoryId, inventoryId)
                    .eq(InventoryRecord::getChangeType, 1);
            List<InventoryRecord> inRecords = inventoryRecordMapper.selectList(inWrapper);
            int inQuantity = inRecords.stream().mapToInt(InventoryRecord::getQuantity).sum();

            // 查询该库存的所有出库记录 (change_type = 2 表示出库)
            LambdaQueryWrapper<InventoryRecord> outWrapper = new LambdaQueryWrapper<>();
            outWrapper.eq(InventoryRecord::getInventoryId, inventoryId)
                    .eq(InventoryRecord::getChangeType, 2);
            List<InventoryRecord> outRecords = inventoryRecordMapper.selectList(outWrapper);
            int outQuantity = outRecords.stream().mapToInt(InventoryRecord::getQuantity).sum();

            // 计算周转率
            double turnoverRate = (inQuantity + inventory.getCurrentStock()) > 0
                    ? (double) outQuantity / (inQuantity + inventory.getCurrentStock()) * 100
                    : 0;

            // 查询商品名称
            String dishName = "未知商品";
            if (dishId != null) {
                Dish dish = dishMapper.selectById(dishId);
                if (dish != null && dish.getDishName() != null) {
                    dishName = dish.getDishName();
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("dishName", dishName);
            item.put("inQuantity", inQuantity);
            item.put("outQuantity", outQuantity);
            item.put("turnoverRate", Math.round(turnoverRate * 100.0) / 100.0);
            list.add(item);
        }

        return Result.success(list);
    }
}
