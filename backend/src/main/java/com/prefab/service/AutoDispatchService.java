package com.prefab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prefab.entity.DeliveryPerson;
import com.prefab.entity.DeliveryTask;
import com.prefab.entity.Order;
import com.prefab.entity.ScheduleTask;
import com.prefab.mapper.DeliveryPersonMapper;
import com.prefab.mapper.DeliveryTaskMapper;
import com.prefab.mapper.OrderMapper;
import com.prefab.mapper.ScheduleTaskMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 自动派单服务
 *
 * <p>下单后立即完成「建调度任务 → 选配送员 → 建配送任务 → 同步订单/配送员状态」全流程，
 * 客户无需等待调度员手工指派人。调度员仍可在 PC 端对 leftover 的订单手动分配。</p>
 *
 * <p>选人规则：排除「休息」中的配送员，只选当前任务数未超过上限的人，
 * 优先任务数最少、其次最早入职的（id 最小），避免把单全压给同一个人。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoDispatchService {

    private final OrderMapper orderMapper;
    private final ScheduleTaskMapper scheduleTaskMapper;
    private final DeliveryTaskMapper deliveryTaskMapper;
    private final DeliveryPersonMapper deliveryPersonMapper;

    /** 下单后是否自动派单，可用 dispatch.auto-assign-on-create=false 关闭 */
    @Getter
    @Value("${dispatch.auto-assign-on-create:true}")
    private boolean autoAssignOnCreate;

    /**
     * 下单后自动派单（受 dispatch.auto-assign-on-create 开关控制）
     */
    public DispatchResult autoAssignOnCreate(Long orderId) {
        if (!autoAssignOnCreate) {
            return DispatchResult.notAssigned("自动派单已关闭，订单进入待调度");
        }
        return autoAssignOrder(orderId);
    }

    /**
     * 给单个订单自动派单
     *
     * @param orderId 订单ID
     * @return 派单结果（未分配时 assigned=false 并给出原因，订单保持「待调度」）
     */
    @Transactional(rollbackFor = Exception.class)
    public DispatchResult autoAssignOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return DispatchResult.notAssigned("订单不存在");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
            return DispatchResult.notAssigned("订单不是待调度状态，跳过自动分配");
        }

        /* 1. 复用或创建调度任务 */
        ScheduleTask task = scheduleTaskMapper.selectOne(new LambdaQueryWrapper<ScheduleTask>()
                .eq(ScheduleTask::getOrderId, orderId)
                .last("limit 1"));
        if (task != null && task.getAssigneeId() != null) {
            return DispatchResult.notAssigned("该订单已分配配送员，跳过");
        }
        if (task == null) {
            task = new ScheduleTask();
            task.setTaskNo(buildNo("SCH"));
            task.setOrderId(orderId);
            task.setDeliveryArea(order.getDeliveryArea());
            task.setExpectedDeliveryTime(order.getExpectedDeliveryTime());
            task.setTaskStatus(0);
            task.setPriority(1);
            task.setRemark("下单自动生成");
            scheduleTaskMapper.insert(task);
        }

        /* 2. 选择配送员 */
        DeliveryPerson person = pickCourier();
        if (person == null) {
            log.warn("订单 {} 自动派单失败：没有可用的配送员（未关联账号或全部休息）", orderId);
            return DispatchResult.notAssigned("暂无可用配送员，已转入调度员待分配");
        }

        /* 3. 指派调度任务 */
        task.setAssigneeId(person.getId());
        task.setTaskStatus(1);
        scheduleTaskMapper.updateById(task);

        /* 4. 生成配送任务 */
        DeliveryTask deliveryTask = new DeliveryTask();
        deliveryTask.setDeliveryNo(buildNo("DEL"));
        deliveryTask.setScheduleTaskId(task.getId());
        deliveryTask.setOrderId(orderId);
        deliveryTask.setDeliveryPersonId(person.getId());
        deliveryTask.setDeliveryAddress(order.getDeliveryAddress());
        deliveryTask.setDeliveryStatus(0);
        deliveryTask.setRemark("下单自动派单");
        deliveryTaskMapper.insert(deliveryTask);

        /* 5. 同步订单状态为已调度 */
        order.setOrderStatus(1);
        orderMapper.updateById(order);

        /* 6. 同步配送员任务数与状态 */
        int currentCount = person.getCurrentTaskCount() == null ? 0 : person.getCurrentTaskCount();
        person.setCurrentTaskCount(currentCount + 1);
        person.setStatus(1);
        deliveryPersonMapper.updateById(person);

        log.info("订单 {} 已自动派单给配送员 {}({})，配送任务 {}", orderId, person.getPersonName(), person.getId(), deliveryTask.getDeliveryNo());
        return DispatchResult.assigned(person.getId(), person.getPersonName(), deliveryTask.getId(), task.getId());
    }

    /**
     * 批量给「待调度」订单自动派单
     *
     * @param limit 单次最多处理多少单，避免一次拉太多
     * @return [成功分配数, 未分配数]
     */
    @Transactional(rollbackFor = Exception.class)
    public int[] autoAssignPendingOrders(int limit) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderStatus, 0)
                .orderByAsc(Order::getCreateTime)
                .last("limit " + Math.max(1, limit)));
        int assigned = 0;
        int failed = 0;
        for (Order order : orders) {
            try {
                DispatchResult result = autoAssignOrder(order.getId());
                if (result.isAssigned()) {
                    assigned++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
                log.error("订单 {} 自动派单异常", order.getId(), e);
            }
        }
        return new int[]{assigned, failed};
    }

    /**
     * 释放订单的派单结果（订单取消 / 配送异常时调用）
     *
     * <p>把未完成的配送任务置为异常、取消调度任务，并把占用的任务数还给配送员，
     * 避免订单取消后配送员端残留「幽灵任务」、任务数长期不回退。</p>
     *
     * @param orderId           订单ID
     * @param reason            写入配送任务备注的原因
     * @param targetOrderStatus 订单目标状态：4-已取消（客户取消）、0-待调度（配送异常，可重新派单）；
     *                          传 null 表示不改订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseOrderDispatch(Long orderId, String reason, Integer targetOrderStatus) {
        List<DeliveryTask> tasks = deliveryTaskMapper.selectList(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getOrderId, orderId));
        for (DeliveryTask task : tasks) {
            if (Integer.valueOf(3).equals(task.getDeliveryStatus())
                    || Integer.valueOf(2).equals(task.getDeliveryStatus())) {
                continue;
            }
            task.setDeliveryStatus(3);
            task.setRemark(reason);
            deliveryTaskMapper.updateById(task);
            releaseCourierCapacity(task.getDeliveryPersonId());
        }

        ScheduleTask task = scheduleTaskMapper.selectOne(new LambdaQueryWrapper<ScheduleTask>()
                .eq(ScheduleTask::getOrderId, orderId)
                .last("limit 1"));
        if (task != null) {
            scheduleTaskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ScheduleTask>()
                    .eq(ScheduleTask::getId, task.getId())
                    .set(ScheduleTask::getTaskStatus, 4)
                    .set(ScheduleTask::getAssigneeId, null));
        }

        if (targetOrderStatus != null) {
            Order order = orderMapper.selectById(orderId);
            if (order != null && !Integer.valueOf(3).equals(order.getOrderStatus())
                    && !Integer.valueOf(4).equals(order.getOrderStatus())) {
                order.setOrderStatus(targetOrderStatus);
                orderMapper.updateById(order);
            }
        }
        log.info("订单 {} 已释放派单，原因：{}", orderId, reason);
    }

    /** 归还一个任务额度：任务数 -1，减到 0 且未休息则回到空闲 */
    private void releaseCourierCapacity(Long personId) {
        if (personId == null) {
            return;
        }
        DeliveryPerson person = deliveryPersonMapper.selectById(personId);
        if (person == null) {
            return;
        }
        int currentCount = person.getCurrentTaskCount() == null ? 0 : person.getCurrentTaskCount();
        person.setCurrentTaskCount(Math.max(0, currentCount - 1));
        if (person.getCurrentTaskCount() == 0 && !Integer.valueOf(2).equals(person.getStatus())) {
            person.setStatus(0);
        }
        deliveryPersonMapper.updateById(person);
    }

    /** 选择一个当前任务数最少的可用配送员 */
    private DeliveryPerson pickCourier() {        List<DeliveryPerson> candidates = deliveryPersonMapper.selectList(new LambdaQueryWrapper<DeliveryPerson>()
                /* 必须已关联登录账号，否则派给他也没人能接单 */
                .isNotNull(DeliveryPerson::getUserId)
                /* 2-休息 不参与派单 */
                .ne(DeliveryPerson::getStatus, 2)
                .orderByAsc(DeliveryPerson::getCurrentTaskCount)
                .orderByAsc(DeliveryPerson::getId)
                .last("limit 1"));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    /** 生成业务单号：前缀 + 时间戳 + 3 位随机数，避免批量时同毫秒重复 */
    private String buildNo(String prefix) {
        return prefix + System.currentTimeMillis() + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    /** 自动派单结果 */
    @Getter
    public static class DispatchResult {
        private final boolean assigned;
        private final String message;
        private final Long deliveryPersonId;
        private final String deliveryPersonName;
        private final Long deliveryTaskId;
        private final Long scheduleTaskId;

        private DispatchResult(boolean assigned, String message, Long deliveryPersonId,
                               String deliveryPersonName, Long deliveryTaskId, Long scheduleTaskId) {
            this.assigned = assigned;
            this.message = message;
            this.deliveryPersonId = deliveryPersonId;
            this.deliveryPersonName = deliveryPersonName;
            this.deliveryTaskId = deliveryTaskId;
            this.scheduleTaskId = scheduleTaskId;
        }

        public static DispatchResult assigned(Long personId, String personName, Long deliveryTaskId, Long scheduleTaskId) {
            return new DispatchResult(true, "已自动分配配送员：" + personName,
                    personId, personName, deliveryTaskId, scheduleTaskId);
        }

        public static DispatchResult notAssigned(String message) {
            return new DispatchResult(false, message, null, null, null, null);
        }
    }
}
