package com.dh.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Category;

import java.util.List;

public interface ICategoryService extends IService<Category> {
    R<IPage> pageList(Integer page, Integer pageSize);

    R<String> addCategory(Category category);

    R<String> update(Category category);

    R<String> delete(Long id);

    R<List<Category>> list(Integer type);
}
