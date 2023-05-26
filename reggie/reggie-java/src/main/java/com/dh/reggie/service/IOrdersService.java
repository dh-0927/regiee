package com.dh.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Orders;

import java.time.LocalDateTime;

public interface IOrdersService extends IService<Orders> {
    R<String> submit(Orders orders);

    R<IPage> userPage(Integer page, Integer pageSize);

    R<IPage> pageList(Integer page, Integer pageSize, Long number, LocalDateTime beginTime, LocalDateTime endTime);

    R<String> editStatus(Orders orders);
}
