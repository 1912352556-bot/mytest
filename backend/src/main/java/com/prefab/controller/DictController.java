package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prefab.common.Result;
import com.prefab.entity.DictData;
import com.prefab.entity.DictType;
import com.prefab.mapper.DictDataMapper;
import com.prefab.mapper.DictTypeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 */
@Tag(name = "数据字典")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;

    // ==================== 字典类型 ====================

    @Operation(summary = "获取字典类型列表")
    @GetMapping("/type/list")
    public Result<List<DictType>> listTypes(
            @RequestParam(required = false) String dictCode,
            @RequestParam(required = false) String dictName) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictCode), DictType::getDictCode, dictCode)
               .like(StringUtils.hasText(dictName), DictType::getDictName, dictName)
               .orderByAsc(DictType::getId);
        List<DictType> types = dictTypeMapper.selectList(wrapper);
        return Result.success(types);
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/type/{id}")
    public Result<DictType> getTypeDetail(@PathVariable Long id) {
        DictType type = dictTypeMapper.selectById(id);
        return Result.success(type);
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody DictType type) {
        type.setStatus(1);
        dictTypeMapper.insert(type);
        return Result.success();
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/type")
    public Result<Void> updateType(@RequestBody DictType type) {
        dictTypeMapper.updateById(type);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictTypeMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 字典数据 ====================

    @Operation(summary = "根据字典类型ID获取字典数据列表")
    @GetMapping("/data/list/{dictTypeId}")
    public Result<List<DictData>> listDataByTypeId(@PathVariable Long dictTypeId) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictTypeId, dictTypeId)
               .orderByAsc(DictData::getSortOrder);
        List<DictData> data = dictDataMapper.selectList(wrapper);
        return Result.success(data);
    }

    @Operation(summary = "根据字典编码获取字典数据")
    @GetMapping("/data/code/{dictCode}")
    public Result<List<DictData>> getDataByCode(@PathVariable String dictCode) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictCode, dictCode)
               .orderByAsc(DictData::getSortOrder);
        List<DictData> data = dictDataMapper.selectList(wrapper);
        return Result.success(data);
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    public Result<Void> addData(@RequestBody DictData data) {
        data.setStatus(1);
        dictDataMapper.insert(data);
        return Result.success();
    }

    @Operation(summary = "更新字典数据")
    @PutMapping("/data")
    public Result<Void> updateData(@RequestBody DictData data) {
        dictDataMapper.updateById(data);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    public Result<Void> deleteData(@PathVariable Long id) {
        dictDataMapper.deleteById(id);
        return Result.success();
    }
}
