package com.prefab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prefab.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
}
