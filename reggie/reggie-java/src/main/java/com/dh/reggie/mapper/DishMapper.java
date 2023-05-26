package com.dh.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dh.reggie.entiry.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
