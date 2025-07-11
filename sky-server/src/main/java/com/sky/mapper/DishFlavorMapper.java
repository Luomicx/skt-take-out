package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ID删除口味
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 批量删除菜品口味接口
     * @param dishIds
     */
    void deleteByIds(List<Long> dishIds);

    /**
     * 根据菜品ID返回口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
