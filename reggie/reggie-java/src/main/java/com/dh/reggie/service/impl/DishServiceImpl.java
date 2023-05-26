package com.dh.reggie.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.DishDto;
import com.dh.reggie.entiry.Dish;
import com.dh.reggie.entiry.DishFlavor;
import com.dh.reggie.entiry.Setmeal;
import com.dh.reggie.entiry.SetmealDish;
import com.dh.reggie.mapper.DishMapper;
import com.dh.reggie.service.*;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dh.reggie.constants.BackendConstants.DISH_CATEGORY;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements IDishService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IDishFlavorService dishFlavorService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private ISetmealService setmealService;

    @Autowired
    private ISetmealDishService setmealDishService;

    @Override
    public R<IPage> pageList(Integer page, Integer pageSize, String name) {
        IPage<Dish> pages = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();

        lqw.like(StringUtils.isNotBlank(name), Dish::getName, name);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 首先分页排序查出所菜品
        this.page(pages, lqw);


        IPage<DishDto> pageDto = new Page<>();
        BeanUtils.copyProperties(pages, pageDto, "records");

        // 依次根据菜品对应的菜品分类id查出categoryName
        List<DishDto> dtos = pages.getRecords().stream()
                .map(dish -> {
                    String categoryName = categoryService.getById(dish.getCategoryId()).getName();
                    DishDto dishDto = new DishDto();
                    dishDto.setCategoryName(categoryName);
                    BeanUtils.copyProperties(dish, dishDto);
                    return dishDto;
                })
                .collect(Collectors.toList());
        pageDto.setRecords(dtos);

        return R.success(pageDto);

    }

    @Override
    @Transactional
    public R<String> addDish(DishDto dishDto) {
        // 添加菜品时清除该类别下对应的缓存
        String key = DISH_CATEGORY + dishDto.getCategoryId();
        stringRedisTemplate.delete(key);
        if (!this.save(dishDto)) {
            return R.error("添加菜品失败！");
        }
        // 添加成功
        // 为每种口味的dishId赋值
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 如果添加菜品没有添加口味，直接返回
        if (flavors == null) {
            return R.success("添加成功");
        }
        // 如果有口味
        flavors = flavors.stream()
                .map(dishFlavor -> {
                    dishFlavor.setDishId(dishDto.getId());
                    return dishFlavor;
                })
                .collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
        return R.success("添加成功");
    }

    @Override
    public R<DishDto> getDishAndFlavor(Long id) {
        DishDto dishDto = new DishDto();
        Dish dish = getById(id);
        BeanUtils.copyProperties(dish, dishDto);
        // 根据菜品id查询该菜品的口味
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorService.list(lqw);
        dishDto.setFlavors(list);
        return R.success(dishDto);
    }

    @Override
    @Transactional
    public R<String> updateDish(DishDto dishDto) {
        // 更新菜品时清除该类别下对应的缓存
        String key = DISH_CATEGORY + dishDto.getCategoryId();
        stringRedisTemplate.delete(key);
        // 首先更新菜品
        if (!updateById(dishDto)) {
            return R.error("更新菜品失败！");
        }
        // 菜品更新成功后再更新口味
        // 首先删除所有口味
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);

        // 再将修改后的口味添加
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors == null) {
            return R.success("修改成功");
        }
        flavors = flavors.stream()
                .map(dishFlavor -> {
                    dishFlavor.setDishId(dishDto.getId());
                    return dishFlavor;
                })
                .collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

        return R.success("修改成功");
    }

    @Override
    public R<String> deleteByIds(String ids) {

        // 解析出需要删除的菜品id
        List<Long> idList = stringTransList(ids);
        // 判断是否可删除
        // 如果菜品关联有套餐，则不可删除（setmeal_dish表）

        for (Long id : idList) {
            LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(SetmealDish::getDishId, id);
            if (setmealDishService.count(lqw1) > 0) {
                return R.error("菜品关联套餐，无法删除！");
            }
        }
        // 如果走到这，说明可删除

        // 首先删除菜品
        removeByIds(idList);

        LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
        // 再删除口味表中对应的信息
        idList.forEach(id -> {
            lqw2.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(lqw2);
        });
        return R.success("删除成功");
    }

    @Override
    public R<String> modifyStatus(Integer status, String ids) {
        // 同样先解析出ids
        List<Long> idList = stringTransList(ids);

        // 删除缓存
        idList.forEach(id -> {
            String key = DISH_CATEGORY + getById(id).getCategoryId();
            stringRedisTemplate.delete(key);
        });

        // 起售无论何时都可
        // 批量修改菜品的状态为status
        if (status == 1) {
            updateDishStatus(idList, 1);
            return R.success("状态修改成功");
        }
        // 判断是否可删除
        // 如果菜品关联有套餐，则不可删除（setmeal_dish表）


        for (Long id : idList) {
            // 如果套餐包含该菜品并且套餐处于起售状态，则菜品不可改为停售状态
            // 判断该菜品是否在某套餐下
            LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(SetmealDish::getDishId, id);
            List<SetmealDish> list1 = setmealDishService.list(lqw1);
            if (list1.size() != 0) {
                // 判断套餐是否有未停售的
                for (SetmealDish setmealDish : list1) {
                    // 查出套餐

                    Setmeal setmeal = setmealService.getById(setmealDish.getSetmealId());
                    if (setmeal.getStatus() == 1) {
                        // 如果套餐处于起售状态，返回错误信息
                        return R.error("有套餐处于起售，不可删除！");
                    }

                }
            }
        }
        // 走到这里，说明可以将状态改为停售
        updateDishStatus(idList, 0);

        return R.success("状态修改成功");
    }

    private void updateDishStatus(List<Long> idList, Integer status) {
        List<Dish> collect = idList.stream().map(id -> {
            //封装dish对象
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());
        updateBatchById(collect);
    }


    @Override
    public R<List<DishDto>> dishList(Dish dish) {
        String key = DISH_CATEGORY + dish.getCategoryId();
        // 查询缓存
        String dishListJSON = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(dishListJSON)) {
            // 缓存命中，直接返回
            return R.success( JSON.parseArray(dishListJSON, DishDto.class));
        }
        // 未命中，查询数据库
        // 只能返回处于起售状态的菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.like(StringUtils.isNotBlank(dish.getName()), Dish::getName, dish.getName());
        lqw.eq(Dish::getStatus, 1);
        List<Dish> list = list(lqw);

        List<DishDto> dishDtoList = list.stream()
                .map(item -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);
                    LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
                    lqw2.eq(DishFlavor::getDishId, item.getId());
                    List<DishFlavor> flavors = dishFlavorService.list(lqw2);
                    dishDto.setFlavors(flavors);
                    return dishDto;
                }).collect(Collectors.toList());

        // 将菜品或套餐数据存入redis
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(dishDtoList));

        return R.success(dishDtoList);
    }

    private List<Long> stringTransList(String ids) {
        List<Long> idList = Stream.of(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return idList;
    }


}
