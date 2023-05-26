package com.dh.reggie.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Category;
import com.dh.reggie.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/page")
    public R<IPage> pageList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return categoryService.pageList(page, pageSize);
    }

    @PostMapping
    public R<String> addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    @PutMapping
    public R<String> update(@RequestBody Category category) {
        return categoryService.update(category);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam(name = "ids", defaultValue = "") Long id) {
        return categoryService.delete(id);
    }

    @GetMapping("/list")
    public R<List<Category>> list(@RequestParam(value = "type", required = false) Integer type) {
        return categoryService.list(type);
    }
}
