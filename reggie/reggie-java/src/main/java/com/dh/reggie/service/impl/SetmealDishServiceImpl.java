package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.entiry.SetmealDish;
import com.dh.reggie.mapper.SetmealDishMapper;
import com.dh.reggie.service.ISetmealDishService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements ISetmealDishService {
}
