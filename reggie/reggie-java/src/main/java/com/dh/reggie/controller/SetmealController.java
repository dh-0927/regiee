package com.dh.reggie.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.SetmealDto;
import com.dh.reggie.entiry.Setmeal;
import com.dh.reggie.entiry.SetmealDish;
import com.dh.reggie.service.ISetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private ISetmealService setmealService;

    @GetMapping("/page")
    public R<IPage> pageList(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                             @RequestParam(name = "name", defaultValue = "") String name) {
        return setmealService.pageList(page, pageSize, name);
    }

    @PostMapping
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        return setmealService.addSetmeal(setmealDto);
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealAndDishs(@PathVariable("id") Long id) {
        return setmealService.getSetmealAndDishs(id);
    }

    @PutMapping
    public R<String> updateSetmeal(@RequestBody SetmealDto setmealDto) {
        return setmealService.updateSetmeal(setmealDto);
    }

    @DeleteMapping
    public R<String> deleteByIds(@RequestParam("ids") String ids) {
        return setmealService.deleteByIds(ids);
    }

    @PostMapping("/status/{status}")
    public R<String> modifyStatus(@PathVariable("status") Integer status, @RequestParam("ids") String ids) {
        return setmealService.modifyStatus(status, ids);
    }

    @GetMapping("/list")
    public R<List<Setmeal>> listByUser(Setmeal setmeal) {
        return setmealService.listByUser(setmeal);
    }


}
