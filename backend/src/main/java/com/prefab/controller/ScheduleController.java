package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.entity.DeliveryPerson;
import com.prefab.entity.DeliveryTask;
import com.prefab.entity.Order;
import com.prefab.entity.ScheduleTask;
import com.prefab.mapper.DeliveryPersonMapper;
import com.prefab.mapper.DeliveryTaskMapper;
import com.prefab.mapper.OrderMapper;
import com.prefab.mapper.ScheduleTaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 调度管理控制器
 */
@Tag(name = "调度管理")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleTaskMapper scheduleTaskMapper;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final OrderMapper orderMapper;
    private final DeliveryTaskMapper deliveryTaskMapper;
    private final com.prefab.service.AutoDispatchService autoDispatchService;

    @Operation(summary = "获取调度任务列表")
    @GetMapping("/list")
    public Result<PageResult<ScheduleTask>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer taskStatus,
            @RequestParam(required = false) String deliveryArea) {
        Page<ScheduleTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ScheduleTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(taskStatus != null, ScheduleTask::getTaskStatus, taskStatus)
               .like(StringUtils.hasText(deliveryArea), ScheduleTask::getDeliveryArea, deliveryArea)
               .orderByDesc(ScheduleTask::getCreateTime);
        Page<ScheduleTask> result = scheduleTaskMapper.selectPage(page, wrapper);
        
        // 关联查询订单编号和配送员姓名
        result.getRecords().forEach(task -> {
            if (task.getOrderId() != null) {
                Order order = orderMapper.selectById(task.getOrderId());
                if (order != null) {
                    task.setOrderNo(order.getOrderNo());
                }
            }
            if (task.getAssigneeId() != null) {
                DeliveryPerson person = deliveryPersonMapper.selectById(task.getAssigneeId());
                if (person != null) {
                    task.setAssigneeName(person.getPersonName());
                }
            }
        });
        
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取调度任务详情")
    @GetMapping("/{id}")
    public Result<ScheduleTask> getTaskDetail(@PathVariable Long id) {
        ScheduleTask task = scheduleTaskMapper.selectById(id);
        
        // 关联查询订单编号和配送员姓名
        if (task != null) {
            if (task.getOrderId() != null) {
                Order order = orderMapper.selectById(task.getOrderId());
                if (order != null) {
                    task.setOrderNo(order.getOrderNo());
                }
            }
            if (task.getAssigneeId() != null) {
                DeliveryPerson person = deliveryPersonMapper.selectById(task.getAssigneeId());
                if (person != null) {
                    task.setAssigneeName(person.getPersonName());
                }
            }
        }
        
        return Result.success(task);
    }

    @Operation(summary = "创建调度任务")
    @PostMapping
    public Result<ScheduleTask> createTask(@RequestBody ScheduleTask task) {
        // 检查订单是否已存在调度任务
        if (task.getOrderId() != null) {
            LambdaQueryWrapper<ScheduleTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ScheduleTask::getOrderId, task.getOrderId());
            ScheduleTask existTask = scheduleTaskMapper.selectOne(wrapper);
            if (existTask != null) {
                return Result.fail("该订单已存在调度任务");
            }
        }
        
        // 生成任务编号
        task.setTaskNo("SCH" + System.currentTimeMillis());
        task.setTaskStatus(0); // 待分配
        
        // 从订单中获取配送区域和期望配送时间
        if (task.getOrderId() != null) {
            Order order = orderMapper.selectById(task.getOrderId());
            if (order != null) {
                task.setDeliveryArea(order.getDeliveryArea());
                task.setExpectedDeliveryTime(order.getExpectedDeliveryTime());
            }
        }
        
        scheduleTaskMapper.insert(task);
        return Result.success(task);
    }

    @Operation(summary = "批量创建调度任务")
    @PostMapping("/batch")
    public Result<Object> batchCreateTasks(@RequestBody BatchRequest request) {
        if (request == null || request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            return Result.fail("请选择要调度的订单");
        }
        int success = 0;
        int fail = 0;
        int assigned = 0;
        for (Long orderId : request.getOrderIds()) {
            try {
                /* 建任务 + 立即尝试自动分配，分配不到就留在待分配列表 */
                com.prefab.service.AutoDispatchService.DispatchResult result =
                        autoDispatchService.autoAssignOrder(orderId);
                success++;
                if (result.isAssigned()) {
                    assigned++;
                }
            } catch (Exception e) {
                fail++;
            }
        }
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("success", success);
        data.put("fail", fail);
        data.put("assigned", assigned);
        return Result.success(data);
    }

    @Operation(summary = "自动分配调度任务")
    @PostMapping("/auto-assign")
    public Result<Object> autoAssignTasks(@RequestBody(required = false) Object data) {
        int[] result = autoDispatchService.autoAssignPendingOrders(200);
        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("assigned", result[0]);
        summary.put("failed", result[1]);
        return Result.success(summary);
    }

    @Operation(summary = "手动分配调度任务")
    @PutMapping("/assign")
    public Result<Void> assignTask(@RequestBody AssignRequest request) {
        ScheduleTask task = scheduleTaskMapper.selectById(request.getTaskId());
        if (task == null) {
            return Result.fail("调度任务不存在");
        }
        
        // 1. 更新调度任务状态
        task.setAssigneeId(request.getAssigneeId());
        task.setTaskStatus(1); // 已分配
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getRemark() != null) {
            task.setRemark(request.getRemark());
        }
        scheduleTaskMapper.updateById(task);
        
        // 2. 创建配送任务
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            DeliveryTask deliveryTask = new DeliveryTask();
            deliveryTask.setDeliveryNo("DEL" + System.currentTimeMillis());
            deliveryTask.setScheduleTaskId(task.getId());
            deliveryTask.setOrderId(task.getOrderId());
            deliveryTask.setDeliveryPersonId(request.getAssigneeId());
            deliveryTask.setDeliveryAddress(order.getDeliveryAddress());
            deliveryTask.setDeliveryStatus(0); // 待取货
            deliveryTaskMapper.insert(deliveryTask);
            
            // 3. 更新订单状态
            order.setOrderStatus(1); // 已调度
            orderMapper.updateById(order);
        }
        
        // 4. 更新配送员任务数量和状态
        DeliveryPerson person = deliveryPersonMapper.selectById(request.getAssigneeId());
        if (person != null) {
            person.setCurrentTaskCount((person.getCurrentTaskCount() != null ? person.getCurrentTaskCount() : 0) + 1);
            person.setStatus(1); // 配送中
            deliveryPersonMapper.updateById(person);
        }
        
        return Result.success();
    }

    @Operation(summary = "更新调度任务状态")
    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateTaskStatus(@PathVariable Long id, @PathVariable Integer status) {
        ScheduleTask task = scheduleTaskMapper.selectById(id);
        if (task != null) {
            task.setTaskStatus(status);
            scheduleTaskMapper.updateById(task);
        }
        return Result.success();
    }

    @Operation(summary = "取消调度任务")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelTask(@PathVariable Long id, @RequestBody(required = false) Object data) {
        ScheduleTask task = scheduleTaskMapper.selectById(id);
        if (task != null) {
            task.setTaskStatus(4); // 已取消
            scheduleTaskMapper.updateById(task);
            
            // 同步更新订单状态为待调度
            Order order = orderMapper.selectById(task.getOrderId());
            if (order != null && order.getOrderStatus() < 2) { // 只有未完成的订单才能重置
                order.setOrderStatus(0); // 待调度
                orderMapper.updateById(order);
            }
            
            // 如果已分配，需要更新配送员任务数
            if (task.getAssigneeId() != null) {
                DeliveryPerson person = deliveryPersonMapper.selectById(task.getAssigneeId());
                if (person != null) {
                    int currentCount = person.getCurrentTaskCount() != null ? person.getCurrentTaskCount() : 0;
                    person.setCurrentTaskCount(Math.max(0, currentCount - 1));
                    if (person.getCurrentTaskCount() == 0) {
                        person.setStatus(0); // 空闲
                    }
                    deliveryPersonMapper.updateById(person);
                }
            }
        }
        return Result.success();
    }

    @Operation(summary = "获取调度日志")
    @GetMapping("/{taskId}/logs")
    public Result<List<Object>> getScheduleLogs(@PathVariable Long taskId) {
        return Result.success(new ArrayList<>());
    }

    @Operation(summary = "获取待分配任务列表")
    @GetMapping("/pending-assign")
    public Result<List<ScheduleTask>> getPendingAssignTasks() {
        LambdaQueryWrapper<ScheduleTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScheduleTask::getTaskStatus, 0);
        List<ScheduleTask> tasks = scheduleTaskMapper.selectList(wrapper);
        
        // 关联查询订单编号
        tasks.forEach(task -> {
            if (task.getOrderId() != null) {
                Order order = orderMapper.selectById(task.getOrderId());
                if (order != null) {
                    task.setOrderNo(order.getOrderNo());
                }
            }
        });
        
        return Result.success(tasks);
    }

    @Operation(summary = "获取可分配的配送人员列表")
    @GetMapping("/available-persons")
    public Result<List<DeliveryPerson>> getAvailablePersons() {
        /* 可分配 = 已关联登录账号且未休息；按当前任务数升序，方便调度员优先选空闲的人 */
        LambdaQueryWrapper<DeliveryPerson> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(DeliveryPerson::getUserId)
               .ne(DeliveryPerson::getStatus, 2)
               .orderByAsc(DeliveryPerson::getCurrentTaskCount)
               .orderByAsc(DeliveryPerson::getId);
        List<DeliveryPerson> persons = deliveryPersonMapper.selectList(wrapper);
        return Result.success(persons);
    }

    // 内部类
    @lombok.Data
    static class AssignRequest {
        private Long taskId;
        private Long assigneeId;
        private Integer priority;
        private String remark;
    }

    @lombok.Data
    static class BatchRequest {
        private java.util.List<Long> orderIds;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class BatchResult {
        private int success;
        private int fail;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class AssignResult {
        private int assigned;
        private int failed;
    }
}
