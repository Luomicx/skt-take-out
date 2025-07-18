package com.sky.mapper;

import com.sky.entity.User;
import lombok.Setter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 查询
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 根据时间统计人数
     * @param mp
     * @return
     */
    Integer countByMap(Map mp);
}
