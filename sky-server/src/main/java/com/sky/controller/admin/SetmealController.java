/**
 * @author Wiretender
 * @version 1.0
 */
package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO pageQueryDTO) {
        log.info("分页查询套餐，当前页码：{}，每页数量：{}", pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        PageResult pageResult = setmealService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐，ID列表：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐回显数据")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据ID查询套餐，ID：{}", id);
        SetmealVO setmealVO = setmealService.getIdByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 起售/停售套餐
     * @param status 状态：1启用，0禁用
     * @param id 套餐ID
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("起售/停售套餐，ID：{}，状态：{}", id, status);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}