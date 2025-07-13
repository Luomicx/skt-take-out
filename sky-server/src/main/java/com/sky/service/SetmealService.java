/**
 * @author Wiretender
 * @version 1.0
 */
package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据Id查询套餐
     * @param id
     * @return
     */
    SetmealVO getIdByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 套餐停售起售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
