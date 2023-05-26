package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.entiry.DishFlavor;
import com.dh.reggie.mapper.DishFlavorMapper;
import com.dh.reggie.service.IDishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements IDishFlavorService {
}
