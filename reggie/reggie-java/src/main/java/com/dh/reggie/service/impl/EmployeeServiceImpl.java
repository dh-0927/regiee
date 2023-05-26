package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Employee;
import com.dh.reggie.mapper.EmployeeMapper;
import com.dh.reggie.service.IEmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;


import static com.dh.reggie.constants.BackendConstants.ADMIN_ID;
import static com.dh.reggie.constants.BackendConstants.LOGIN_SESSION;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {

    @Override
    public R<Employee> login(HttpServletRequest request, Employee e) {
        // 对输入的登陆信息进行非空判断
        // 即使前端进行了非空判断，后端也需要进行数据校验。
        // 这是因为前端校验可以被绕过，用户可以使用浏览器的调试工具等方式，直接修改请求参数来进行攻击。
        if (e == null || StringUtils.isBlank(e.getUsername()) || StringUtils.isBlank(e.getPassword())) {
            return R.error("错误的用户信息！");
        }

        // 将用户输入的密码进行加密处理，准备与查询到数据库中的进行对比
        String password = DigestUtils.md5DigestAsHex(e.getPassword().getBytes());

        // 根据用户名查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername, e.getUsername());
        Employee emp = getOne(lqw);

        // 进行判断
        // 如果为空，返回登陆错误信息
        if (emp == null) {
            return R.error("登陆失败！");
        }

        // 如果不为空，判断密码是否正确
        // 如果错误，返回错误信息
        if (!password.equals(emp.getPassword())) {
            return R.error("登陆失败！");
        }

        // 如果密码正确，判断用户是否被禁用
        // 如果被禁用，返回错误信息
        if (emp.getStatus() != 1) {
            return R.error("该用户被禁用！");
        }

        // 如果未被禁用，先将用户id存入session, 然后返回登陆成功信息，并将用户信息返回，
        request.getSession().setAttribute(LOGIN_SESSION, emp.getId());
        return R.success(emp);
    }

    @Override
    public R<String> logout(HttpServletRequest request) {
        // 直接将session中保存的employee删除
        request.getSession().removeAttribute(LOGIN_SESSION);
        return R.success("删除成功。");
    }

    @Override
    public R<String> addEmp(Employee e) {
        // 设置初始密码（123456）
        e.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 直接进行添加，如果已存在账户直接报错，在全局异常类中直接返回错误信息
        save(e);
        return R.success("添加用户" + e.getName() + "成功。");
    }

    @Override
    public R<IPage> pageList(Integer page, Integer pageSize, String name) {
        LambdaQueryWrapper<Employee> lqw1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotBlank(name), Employee::getName, name);
        lqw.orderByDesc(Employee::getUpdateTime);
        IPage<Employee> iPage = new Page<>(page, pageSize);
        page(iPage, lqw);
        return R.success(iPage);
    }

//    private String rootUsername = "admin";
    @Override
    public R<String> update(Employee e) {
        // 判断是不是更新admin
        if (e.getId() != ADMIN_ID) {
            // 更新数据库
            updateById(e);
            // 返回成功信息
            return R.success("员工信息修改成功！");
        }
        // 如果是更新admin
        // 判断是否禁用
        if (e.getStatus() == 0) {
            return R.error("不可禁用管理员！");
        }

        // 如果是正常更新
        // 判断账号是否更改
//        e.getUsername()
        updateById(e);
        return R.success("修改成功");

    }

}
