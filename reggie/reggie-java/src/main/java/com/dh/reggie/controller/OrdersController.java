package com.dh.reggie.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Orders;
import com.dh.reggie.service.IOrdersService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        return ordersService.submit(orders);
    }

    @GetMapping("/userPage")
    public R<IPage> userPage(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "1") Integer pageSize) {
        return ordersService.userPage(page, pageSize);
    }

    @GetMapping("/page")
    public R<IPage> pageList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "1") Integer pageSize,
                             @RequestParam(name = "number", defaultValue = "") Long number,
                             @RequestParam(name = "beginTime", defaultValue = "") String beginTime,
                             @RequestParam(name = "endTime", defaultValue = "") String endTime) {

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime)) {
            return ordersService.pageList(page, pageSize, number, null, null);
        }

        LocalDateTime begin = LocalDateTime.parse(beginTime, pattern);
        LocalDateTime end = LocalDateTime.parse(endTime, pattern);


        return ordersService.pageList(page, pageSize, number, begin, end);

    }

    @PutMapping
    public R<String> editStatus(@RequestBody Orders orders) {
        return ordersService.editStatus(orders);
    }



}
