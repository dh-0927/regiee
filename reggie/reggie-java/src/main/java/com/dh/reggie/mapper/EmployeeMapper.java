package com.dh.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dh.reggie.entiry.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee>{
}
