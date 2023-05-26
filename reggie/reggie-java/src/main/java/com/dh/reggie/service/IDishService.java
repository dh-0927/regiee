package com.dh.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.DishDto;
import com.dh.reggie.entiry.Dish;

import java.util.List;

public interface IDishService extends IService<Dish> {
    R<IPage> pageList(Integer page, Integer pageSize, String name);

    R<String> addDish(DishDto dishDto);

    R<DishDto> getDishAndFlavor(Long id);

    R<String> updateDish(DishDto dishDto);

    R<String> deleteByIds(String ids);

    R<String> modifyStatus(Integer status, String ids);

    R<List<DishDto>> dishList(Dish dish);

}
