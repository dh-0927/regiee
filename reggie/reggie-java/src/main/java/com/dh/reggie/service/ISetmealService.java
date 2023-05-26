package com.dh.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.SetmealDto;
import com.dh.reggie.entiry.Setmeal;

import java.util.List;

public interface ISetmealService extends IService<Setmeal> {
    R<IPage> pageList(Integer page, Integer pageSize, String name);

    R<String> addSetmeal(SetmealDto setmealDto);

    R<SetmealDto> getSetmealAndDishs(Long id);

    R<String> updateSetmeal(SetmealDto setmealDto);

    R<String> deleteByIds(String ids);

    R<String> modifyStatus(Integer status, String ids);

    R<List<Setmeal>> listByUser(Setmeal setmeal);
}
