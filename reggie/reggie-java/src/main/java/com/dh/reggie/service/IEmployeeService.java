package com.dh.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Employee;

import javax.servlet.http.HttpServletRequest;

public interface IEmployeeService extends IService<Employee> {
    R<Employee> login(HttpServletRequest request, Employee e);

    R<String> logout(HttpServletRequest request);

    R<String> addEmp(Employee e);

    R<IPage> pageList(Integer page, Integer pageSize, String name);

    R<String> update(Employee e);

}
