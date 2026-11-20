package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.entity.*;
import com.prefab.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 配送管理控制器
 */
@Tag(name = "配送管理")
@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryTaskMapper deliveryTaskMapper;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final OrderMapper orderMapper;
    private final ScheduleTaskMapper scheduleTaskMapper;
    private final OrderItemMapper orderItemMapper;
    private final InventoryMapper inventoryMapper;
    private final DishMapper dishMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    // ==================== 配送人员 ====================

    @Operation(summary = "获取配送人员列表")
    @GetMapping("/person/list")
    public Result<PageResult<DeliveryPerson>> listPersons(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String personName,
            @RequestParam(required = false) Integer status) {
        Page<DeliveryPerson> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DeliveryPerson> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(personName), DeliveryPerson::getPersonName, personName)
               .eq(status != null, DeliveryPerson::getStatus, status)
               .orderByDesc(DeliveryPerson::getCreateTime);
        Page<DeliveryPerson> result = deliveryPersonMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取配送人员详情")
    @GetMapping("/person/{id}")
    public Result<DeliveryPerson> getPersonDetail(@PathVariable Long id) {
        DeliveryPerson person = deliveryPersonMapper.selectById(id);
        return Result.success(person);
    }

    @Operation(summary = "新增配送人员")
    @PostMapping("/person")
    public Result<Void> addPerson(@RequestBody DeliveryPerson person) {
        person.setStatus(0); // 空闲
        deliveryPersonMapper.insert(person);
        return Result.success();
    }

    @Operation(summary = "更新配送人员")
    @PutMapping("/person")
    public Result<Void> updatePerson(@RequestBody DeliveryPerson person) {
        deliveryPersonMapper.updateById(person);
        return Result.success();
    }

    @Operation(summary = "删除配送人员")
    @DeleteMapping("/person/{id}")
    public Result<Void> deletePerson(@PathVariable Long id) {
        deliveryPersonMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "修改配送人员状态")
    @PutMapping("/person/{id}/status/{status}")
    public Result<Void> updatePersonStatus(@PathVariable Long id, @PathVariable Integer status) {
        DeliveryPerson person = deliveryPersonMapper.selectById(id);
        if (person != null) {
            person.setStatus(status);
            deliveryPersonMapper.updateById(person);
        }
        return Result.success();
    }

    @Operation(summary = "获取空闲配送人员")
    @GetMapping("/person/idle")
    public Result<List<DeliveryPerson>> getIdlePersons() {
        LambdaQueryWrapper<DeliveryPerson> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryPerson::getStatus, 0);
        List<DeliveryPerson> persons = deliveryPersonMapper.selectList(wrapper);
        return Result.success(persons);
    }

    // ==================== 配送任务 ====================

    @Operation(summary = "获取配送任务列表")
    @GetMapping("/task/list")
    public Result<PageResult<DeliveryTask>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer deliveryStatus,
            @RequestParam(required = false) Long deliveryPersonId) {
        Page<DeliveryTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(deliveryStatus != null, DeliveryTask::getDeliveryStatus, deliveryStatus)
               .eq(deliveryPersonId != null, DeliveryTask::getDeliveryPersonId, deliveryPersonId)
               .orderByDesc(DeliveryTask::getCreateTime);
        Page<DeliveryTask> result = deliveryTaskMapper.selectPage(page, wrapper);
        
        // 关联查询订单编号、配送员姓名等信息
        result.getRecords().forEach(task -> {
            if (task.getOrderId() != null) {
                Order order = orderMapper.selectById(task.getOrderId());
                if (order != null) {
                    task.setOrderNo(order.getOrderNo());
                    task.setCustomerName(order.getCustomerName());
                    task.setCustomerPhone(order.getCustomerPhone());
                }
            }
            if (task.getDeliveryPersonId() != null) {
                DeliveryPerson person = deliveryPersonMapper.selectById(task.getDeliveryPersonId());
                if (person != null) {
                    task.setDeliveryPersonName(person.getPersonName());
                }
            }
        });
        
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取配送任务详情")
    @GetMapping("/task/{id}")
    public Result<DeliveryTask> getTaskDetail(@PathVariable Long id) {
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        
        // 关联查询订单编号、配送员姓名等信息
        if (task != null) {
            if (task.getOrderId() != null) {
                Order order = orderMapper.selectById(task.getOrderId());
                if (order != null) {
                    task.setOrderNo(order.getOrderNo());
                    task.setCustomerName(order.getCustomerName());
                    task.setCustomerPhone(order.getCustomerPhone());
                }
            }
            if (task.getDeliveryPersonId() != null) {
                DeliveryPerson person = deliveryPersonMapper.selectById(task.getDeliveryPersonId());
                if (person != null) {
                    task.setDeliveryPersonName(person.getPersonName());
                }
            }
        }
        
        return Result.success(task);
    }

    @Operation(summary = "创建配送任务")
    @PostMapping("/task")
    public Result<DeliveryTask> createTask(@RequestBody DeliveryTask task) {
        task.setDeliveryStatus(0); // 待取货
        deliveryTaskMapper.insert(task);
        return Result.success(task);
    }

    @Operation(summary = "开始配送")
    @PutMapping("/task/{id}/start")
    public Result<Void> startDelivery(@PathVariable Long id) {
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        if (task != null) {
            task.setDeliveryStatus(1); // 配送中
            task.setPickupTime(LocalDateTime.now()); // 设置取货时间
            deliveryTaskMapper.updateById(task);
            
            // 同步更新订单状态
            Order order = orderMapper.selectById(task.getOrderId());
            if (order != null) {
                order.setOrderStatus(2); // 配送中
                orderMapper.updateById(order);
            }
            
            // 同步更新调度任务状态
            ScheduleTask scheduleTask = scheduleTaskMapper.selectById(task.getScheduleTaskId());
            if (scheduleTask != null) {
                scheduleTask.setTaskStatus(2); // 执行中
                scheduleTaskMapper.updateById(scheduleTask);
            }
        }
        return Result.success();
    }

    @Operation(summary = "完成配送")
    @PutMapping("/task/complete")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> completeDelivery(@RequestBody CompleteRequest request) {
        DeliveryTask task = deliveryTaskMapper.selectById(request.getDeliveryTaskId());
        if (task == null) {
            return Result.fail("配送任务不存在");
        }
        
        // 1. 更新配送任务状态
        task.setDeliveryStatus(2); // 已送达
        task.setDeliveryTime(LocalDateTime.now()); // 设置送达时间
        deliveryTaskMapper.updateById(task);
        
        // 2. 更新订单状态
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setOrderStatus(3); // 已完成
            order.setActualDeliveryTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
        
        // 3. 更新调度任务状态
        ScheduleTask scheduleTask = scheduleTaskMapper.selectById(task.getScheduleTaskId());
        if (scheduleTask != null) {
            scheduleTask.setTaskStatus(3); // 已完成
            scheduleTaskMapper.updateById(scheduleTask);
        }
        
        // 4. 更新配送员任务数量和状态
        DeliveryPerson person = deliveryPersonMapper.selectById(task.getDeliveryPersonId());
        if (person != null) {
            int currentCount = person.getCurrentTaskCount() != null ? person.getCurrentTaskCount() : 0;
            person.setCurrentTaskCount(Math.max(0, currentCount - 1)); // 任务数-1
            person.setTotalDeliveryCount((person.getTotalDeliveryCount() != null ? person.getTotalDeliveryCount() : 0) + 1); // 总配送次数+1
            
            if (person.getCurrentTaskCount() == 0) {
                person.setStatus(0); // 空闲
            }
            deliveryPersonMapper.updateById(person);
        }
        
        return Result.success();
    }

    @Operation(summary = "异常上报")
    @PutMapping("/task/{id}/exception")
    public Result<Void> reportException(@PathVariable Long id, @RequestBody(required = false) Object data) {
        DeliveryTask task = deliveryTaskMapper.selectById(id);
        if (task != null) {
            task.setDeliveryStatus(3); // 异常
            deliveryTaskMapper.updateById(task);
        }
        return Result.success();
    }

    @Operation(summary = "获取配送轨迹")
    @GetMapping("/task/{taskId}/track")
    public Result<List<Object>> getDeliveryTrack(@PathVariable Long taskId) {
        return Result.success(new ArrayList<>());
    }

    @Operation(summary = "上报配送位置")
    @PostMapping("/task/location")
    public Result<Void> reportLocation(@RequestBody Object data) {
        return Result.success();
    }
    
    // 内部类
    @lombok.Data
    static class CompleteRequest {
        private Long deliveryTaskId;
    }
}
