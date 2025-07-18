package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入新订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据ID查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据状态查询数量
     * @param status
     * @return
     */
    @Select("select count(id) from orders where id = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据订单状态和下单时间返回超时订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /**
     * 获取订单金额总和
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 获取订单统计
     * @param mp
     * @return
     */
    Integer countByMap(Map mp);

    /**
     * 获取TOP10
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSaleTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
