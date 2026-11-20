package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.entity.Dish;
import com.prefab.entity.DishCategory;
import com.prefab.entity.Inventory;
import com.prefab.mapper.DishMapper;
import com.prefab.mapper.DishCategoryMapper;
import com.prefab.mapper.InventoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生鲜商品控制器
 *
 * <p>提供生鲜商品的增删改查、上下架以及商品分类维护能力。</p>
 */
@Tag(name = "生鲜商品管理")
@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishMapper dishMapper;
    private final DishCategoryMapper categoryMapper;
    private final InventoryMapper inventoryMapper;

    @Operation(summary = "获取商品列表")
    @GetMapping("/list")
    public Result<PageResult<Dish>> listDishes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dishName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer storageType,
            @RequestParam(required = false) Integer status) {
        Page<Dish> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dishName), Dish::getDishName, dishName)
               .eq(categoryId != null, Dish::getCategoryId, categoryId)
               .eq(storageType != null, Dish::getStorageType, storageType)
               .eq(status != null, Dish::getStatus, status)
               .orderByDesc(Dish::getCreateTime);
        Page<Dish> result = dishMapper.selectPage(page, wrapper);

        // 关联查询库存信息
        result.getRecords().forEach(dish -> {
            LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(Inventory::getDishId, dish.getId());
            Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
            if (inventory != null) {
                // 使用库存表的库存数据
                dish.setStock(inventory.getCurrentStock());
            }
        });

        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<Dish> getDishDetail(@PathVariable Long id) {
        Dish dish = dishMapper.selectById(id);

        // 关联查询库存信息
        if (dish != null) {
            LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(Inventory::getDishId, dish.getId());
            Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
            if (inventory != null) {
                // 使用库存表的库存数据
                dish.setStock(inventory.getCurrentStock());
            }
        }

        return Result.success(dish);
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Void> addDish(@RequestBody Dish dish) {
        // 生成商品编码：使用时间戳
        if (!StringUtils.hasText(dish.getDishCode())) {
            dish.setDishCode("DISH" + System.currentTimeMillis());
        }
        dish.setStatus(1);
        dishMapper.insert(dish);

        // 同步创建库存记录
        Inventory inventory = new Inventory();
        inventory.setDishId(dish.getId());
        inventory.setCurrentStock(dish.getStock() != null ? dish.getStock() : 0);
        inventory.setWarningThreshold(100); // 默认预警阈值（按商品单位计）
        inventory.setUnit(dish.getUnit());
        inventory.setStatus(1);
        inventoryMapper.insert(inventory);

        return Result.success();
    }

    @Operation(summary = "更新商品")
    @PutMapping
    public Result<Void> updateDish(@RequestBody Dish dish) {
        dishMapper.updateById(dish);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dishMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "修改商品状态")
    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateDishStatus(@PathVariable Long id, @PathVariable Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish != null) {
            dish.setStatus(status);
            dishMapper.updateById(dish);
        }
        return Result.success();
    }

    @Operation(summary = "获取有效商品列表")
    @GetMapping("/active")
    public Result<List<Dish>> getActiveDishes() {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1);
        List<Dish> dishes = dishMapper.selectList(wrapper);

        // 关联查询库存信息
        dishes.forEach(dish -> {
            LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(Inventory::getDishId, dish.getId());
            Inventory inventory = inventoryMapper.selectOne(inventoryWrapper);
            if (inventory != null) {
                // 使用库存表的库存数据
                dish.setStock(inventory.getCurrentStock());
            }
        });

        return Result.success(dishes);
    }

    // ==================== 商品分类 ====================

    @Operation(summary = "获取商品分类列表")
    @GetMapping("/category/list")
    public Result<List<DishCategory>> listCategories() {
        List<DishCategory> categories = categoryMapper.selectList(null);
        return Result.success(categories);
    }

    @Operation(summary = "新增商品分类")
    @PostMapping("/category")
    public Result<Void> addCategory(@RequestBody DishCategory category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        return Result.success();
    }

    @Operation(summary = "更新商品分类")
    @PutMapping("/category")
    public Result<Void> updateCategory(@RequestBody DishCategory category) {
        categoryMapper.updateById(category);
        return Result.success();
    }

    @Operation(summary = "删除商品分类")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }
}
