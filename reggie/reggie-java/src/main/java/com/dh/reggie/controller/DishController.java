package com.dh.reggie.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.DishDto;
import com.dh.reggie.entiry.Dish;
import com.dh.reggie.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private IDishService dishService;

    @GetMapping("/page")
    public R<IPage> pageList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                             @RequestParam(name = "name", defaultValue = "") String name) {
        return dishService.pageList(page, pageSize, name);
    }

    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto) {
        return dishService.addDish(dishDto);
    }

    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto) {
        return dishService.updateDish(dishDto);
    }

    @GetMapping("{id}")
    public R<DishDto> getDishAndFlavor(@PathVariable("id") Long id) {
        return dishService.getDishAndFlavor(id);
    }

    @DeleteMapping
    public R<String> deleteDish(@RequestParam("ids") String ids) {
        return dishService.deleteByIds(ids);
    }

    @PostMapping("/status/{status}")
    public R<String> modifyStatus(@PathVariable Integer status, @RequestParam String ids) {
        return dishService.modifyStatus(status, ids);
    }

    @GetMapping("/list")
    public R<List<DishDto>> dishList(Dish dish) {
        return dishService.dishList(dish);
    }



}
