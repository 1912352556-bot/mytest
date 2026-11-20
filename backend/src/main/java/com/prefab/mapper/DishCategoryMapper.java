package com.prefab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prefab.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生鲜商品分类Mapper
 */
@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}
