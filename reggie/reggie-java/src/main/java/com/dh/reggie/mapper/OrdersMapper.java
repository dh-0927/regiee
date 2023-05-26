package com.dh.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dh.reggie.entiry.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
