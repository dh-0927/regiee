package com.dh.reggie.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Employee;
import com.dh.reggie.service.IEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private IEmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee e, HttpServletRequest request) {
        return employeeService.login(request, e);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        return employeeService.logout(request);
    }

    @PostMapping
    public R<String> addEmp(@RequestBody Employee e) {
        return employeeService.addEmp(e);
    }

    @GetMapping("/page")
    public R<IPage> pageList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                             @RequestParam(name = "name", defaultValue = "") String name) {
        return employeeService.pageList(page, pageSize, name);
    }

    @PutMapping
    public R<String> update(@RequestBody Employee e) {
        return employeeService.update(e);
    }

    @GetMapping("/{id}")
    public R<Employee> selectById(@PathVariable Long id) {
        return R.success(employeeService.getById(id));

    }
}
