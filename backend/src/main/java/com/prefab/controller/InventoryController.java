package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.entity.Inventory;
import com.prefab.entity.InventoryRecord;
import com.prefab.entity.Dish;
import com.prefab.mapper.InventoryMapper;
import com.prefab.mapper.InventoryRecordMapper;
import com.prefab.mapper.DishMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理控制器
 */
@Tag(name = "库存管理")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final DishMapper dishMapper;

    @Operation(summary = "获取库存列表")
    @GetMapping("/list")
    public Result<PageResult<Inventory>> listInventory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long dishId) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dishId != null, Inventory::getDishId, dishId)
               .orderByDesc(Inventory::getCreateTime);
        Page<Inventory> result = inventoryMapper.selectPage(page, wrapper);
        
        // 关联查询商品名称和编码
        result.getRecords().forEach(inventory -> {
            if (inventory.getDishId() != null) {
                Dish dish = dishMapper.selectById(inventory.getDishId());
                if (dish != null) {
                    inventory.setDishName(dish.getDishName());
                    inventory.setDishCode(dish.getDishCode());
                }
            }
        });
        
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取库存详情")
    @GetMapping("/{id}")
    public Result<Inventory> getInventoryDetail(@PathVariable Long id) {
        Inventory inventory = inventoryMapper.selectById(id);
        
        // 关联查询商品名称和编码
        if (inventory != null && inventory.getDishId() != null) {
            Dish dish = dishMapper.selectById(inventory.getDishId());
            if (dish != null) {
                inventory.setDishName(dish.getDishName());
                inventory.setDishCode(dish.getDishCode());
            }
        }
        
        return Result.success(inventory);
    }

    @Operation(summary = "新增库存记录")
    @PostMapping
    public Result<Void> addInventory(@RequestBody Inventory inventory) {
        inventory.setStatus(1);
        inventoryMapper.insert(inventory);
        return Result.success();
    }

    @Operation(summary = "更新库存信息")
    @PutMapping
    public Result<Void> updateInventory(@RequestBody Inventory inventory) {
        inventoryMapper.updateById(inventory);
        return Result.success();
    }

    @Operation(summary = "删除库存记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteInventory(@PathVariable Long id) {
        inventoryMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "入库操作")
    @PostMapping("/stock-in")
    public Result<Void> stockIn(@RequestBody StockChangeRequest request) {
        // 只更新 t_inventory 表的库存
        Inventory inventory = inventoryMapper.selectById(request.getInventoryId());
        if (inventory == null) {
            return Result.fail("库存记录不存在");
        }
        
        int beforeStock = inventory.getCurrentStock();
        inventory.setCurrentStock(inventory.getCurrentStock() + request.getQuantity());
        inventory.setLastInTime(java.time.LocalDateTime.now());
        inventoryMapper.updateById(inventory);
        
        // 记录入库日志
        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(inventory.getId());
        record.setChangeType(0); // 0-入库
        record.setQuantity(request.getQuantity());
        record.setBeforeStock(beforeStock);
        record.setAfterStock(inventory.getCurrentStock());
        record.setRemark(request.getRemark());
        record.setOperatorId(1L); // TODO: 从登录上下文获取
        inventoryRecordMapper.insert(record);
        
        return Result.success();
    }

    @Operation(summary = "出库操作")
    @PostMapping("/stock-out")
    public Result<Void> stockOut(@RequestBody StockChangeRequest request) {
        Inventory inventory = inventoryMapper.selectById(request.getInventoryId());
        if (inventory == null) {
            return Result.fail("库存不存在");
        }
        
        // 检查库存是否足够
        if (inventory.getCurrentStock() < request.getQuantity()) {
            return Result.fail("库存不足，当前库存：" + inventory.getCurrentStock());
        }
        
        int beforeStock = inventory.getCurrentStock();
        inventory.setCurrentStock(inventory.getCurrentStock() - request.getQuantity());
        inventory.setLastOutTime(java.time.LocalDateTime.now());
        inventoryMapper.updateById(inventory);
        
        // 记录出库日志
        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(inventory.getId());
        record.setChangeType(1); // 1-出库
        record.setQuantity(request.getQuantity());
        record.setBeforeStock(beforeStock);
        record.setAfterStock(inventory.getCurrentStock());
        record.setRemark(request.getRemark());
        record.setOperatorId(1L); // TODO: 从登录上下文获取
        inventoryRecordMapper.insert(record);
        
        return Result.success();
    }

    @Operation(summary = "获取库存变更记录")
    @GetMapping("/{inventoryId}/records")
    public Result<PageResult<InventoryRecord>> listRecords(
            @PathVariable Long inventoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<InventoryRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getInventoryId, inventoryId)
               .orderByDesc(InventoryRecord::getCreateTime);
        Page<InventoryRecord> result = inventoryRecordMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取库存预警列表")
    @GetMapping("/warning")
    public Result<List<Inventory>> getWarningInventory() {
        List<Inventory> inventories = inventoryMapper.selectList(null);
        List<Inventory> warnings = inventories.stream()
                .filter(i -> i.getCurrentStock() < i.getWarningThreshold())
                .toList();
        
        // 关联查询商品名称和编码
        warnings.forEach(inventory -> {
            if (inventory.getDishId() != null) {
                Dish dish = dishMapper.selectById(inventory.getDishId());
                if (dish != null) {
                    inventory.setDishName(dish.getDishName());
                    inventory.setDishCode(dish.getDishCode());
                }
            }
        });
        
        return Result.success(warnings);
    }

    @Operation(summary = "设置预警阈值")
    @PutMapping("/{id}/threshold/{threshold}")
    public Result<Void> setWarningThreshold(@PathVariable Long id, @PathVariable Integer threshold) {
        Inventory inventory = inventoryMapper.selectById(id);
        if (inventory != null) {
            inventory.setWarningThreshold(threshold);
            inventoryMapper.updateById(inventory);
        }
        return Result.success();
    }

    @Operation(summary = "批量设置预警阈值")
    @PutMapping("/threshold/batch")
    public Result<Void> batchSetThreshold(@RequestBody BatchThresholdRequest request) {
        for (Long id : request.getIds()) {
            Inventory inventory = inventoryMapper.selectById(id);
            if (inventory != null) {
                inventory.setWarningThreshold(request.getThreshold());
                inventoryMapper.updateById(inventory);
            }
        }
        return Result.success();
    }

    // 内部类
    @lombok.Data
    static class StockChangeRequest {
        private Long inventoryId;
        private Integer quantity;
        private String remark;
    }

    @lombok.Data
    static class BatchThresholdRequest {
        private List<Long> ids;
        private Integer threshold;
    }
}
