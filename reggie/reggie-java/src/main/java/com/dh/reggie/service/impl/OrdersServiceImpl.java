package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.BaseContext;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.OrdersDto;
import com.dh.reggie.entiry.*;
import com.dh.reggie.mapper.OrdersMapper;
import com.dh.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private IShoppingCartService shoppingCartService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IAddressBookService addressBookService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Override
    @Transactional
    public R<String> submit(Orders orders) {
        if (orders == null) {
            return R.error("错误操作！");
        }
        // 获取当前用户id
        Long userId = BaseContext.get();
        // 查询当前用户的购物车信息
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> carts = shoppingCartService.list(lqw);
        // 生成订单id，用于完善订单表和订单详情表的数据添加
        long orderId = IdWorker.getId();
        // 遍历购物车中的套餐或菜品，使用stream流映射为订单详情实体，同时计算出总金额
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> detailList = carts.stream()
                .map(item -> {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrderId(orderId);
                    orderDetail.setNumber(item.getNumber());
                    orderDetail.setDishFlavor(item.getDishFlavor());
                    orderDetail.setDishId(item.getDishId());
                    orderDetail.setSetmealId(item.getSetmealId());
                    orderDetail.setName(item.getName());
                    orderDetail.setImage(item.getImage());
                    orderDetail.setAmount(item.getAmount());
                    amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
                    return orderDetail;
                })
                .collect(Collectors.toList());
        // 查询出地址表中的信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        // 查询出用户表的信息
        User user = userService.getById(userId);
        // 完善订单表的信息
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 向订单表中插入一条数据
        if (!save(orders)) {
            return R.error("生成订单失败！");
        }
        // 向订单详情表中插入多条数据
        if (!orderDetailService.saveBatch(detailList)) {
            return R.error("生成订单失败！");
        }
        // 删除购物车中的数据
        if (!shoppingCartService.remove(lqw)) {
            return R.error("生成订单失败！");
        }
        return R.success("生成订单成功");
    }

    @Override
    public R<IPage> userPage(Integer page, Integer pageSize) {
        // 获取当前用户Id
        Long userId = BaseContext.get();

        // 查询当前用户的分页数据，并根据时间倒序
        IPage<Orders> ordersIPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, userId).orderByDesc(Orders::getOrderTime);
        page(ordersIPage, lqw);

        // 将分页的基础数据封装进IPage<OrdersDto>中
        IPage<OrdersDto> ordersDtoIPage = new Page<>();
        // 属性拷贝
        BeanUtils.copyProperties(ordersIPage, ordersDtoIPage, "records");

        // 将List<Orders>映射为List<OrdersDto>
        List<OrdersDto> ordersDtoList = ordersIPage.getRecords().stream()
                .map(order -> {
                    // 准备对象
                    OrdersDto ordersDto = new OrdersDto();
                    // 属性拷贝
                    BeanUtils.copyProperties(order, ordersDto);
                    // 查询出订单对应的订单详情
                    LambdaQueryWrapper<OrderDetail> lqwByDetail = new LambdaQueryWrapper<>();
                    lqwByDetail.eq(OrderDetail::getOrderId, order.getNumber());
                    List<OrderDetail> detailList = orderDetailService.list(lqwByDetail);
                    ordersDto.setOrderDetails(detailList);
                    return ordersDto;
                }).collect(Collectors.toList());

        // 为分页对象的records属性赋值
        ordersDtoIPage.setRecords(ordersDtoList);

        // 返回分页对象
        return R.success(ordersDtoIPage);
    }

    @Override
    public R<IPage> pageList(Integer page, Integer pageSize, Long number, LocalDateTime beginTime, LocalDateTime endTime) {

        IPage<Orders> ordersIPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(number != null, Orders::getNumber, number);
        lqw.ge(beginTime != null, Orders::getOrderTime, beginTime);
        lqw.le(endTime != null, Orders::getOrderTime, endTime);
        lqw.orderByDesc(Orders::getOrderTime);

        page(ordersIPage, lqw);


        return R.success(ordersIPage);
    }

    @Override
    public R<String> editStatus(Orders orders) {
        if (!updateById(orders)) {
            return R.error("修改状态失败！");
        }
        return R.success("修改成功");
    }
}
