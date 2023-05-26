package com.dh.reggie.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.R;
import com.dh.reggie.dto.DishDto;
import com.dh.reggie.dto.SetmealDto;
import com.dh.reggie.entiry.Setmeal;
import com.dh.reggie.entiry.SetmealDish;
import com.dh.reggie.mapper.SetmealMapper;
import com.dh.reggie.service.ICategoryService;
import com.dh.reggie.service.ISetmealDishService;
import com.dh.reggie.service.ISetmealService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dh.reggie.constants.BackendConstants.DISH_CATEGORY;
import static com.dh.reggie.constants.BackendConstants.SETMEAL_CATEGORY;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements ISetmealService {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private ISetmealDishService setmealDishService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public R<IPage> pageList(Integer page, Integer pageSize, String name) {
        // 首先将Setmeal中查出的分页信息封装进pageInfo中
        IPage<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(Setmeal::getName, name).orderByDesc(Setmeal::getUpdateTime);
        page(pageInfo, lqw);

        // 将pageInfo中的一些信息copy进pageDto
        IPage<SetmealDto> pageDto = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        // 去category中查询出套餐的分类名称
        List<SetmealDto> list = pageInfo.getRecords().stream()
                .map(setmeal -> {
                    // 将setmeal中的套餐信息copy进setmealDto中
                    SetmealDto setmealDto = new SetmealDto();
                    BeanUtils.copyProperties(setmeal, setmealDto);
                    // 接下来为setmealDto中的categoryName属性赋值
                    String categoryName = categoryService.getById(setmeal.getCategoryId()).getName();
                    setmealDto.setCategoryName(categoryName);
                    // 返回setmealDto
                    return setmealDto;
                })
                .collect(Collectors.toList());
        // 为pageDto中的records集合赋值
        pageDto.setRecords(list);
        return R.success(pageDto);
    }

    /**
     * 添加套餐
     * 需要先操作setmeal表
     * 再操作setmeal_dish表
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public R<String> addSetmeal(SetmealDto setmealDto) {

        String key = SETMEAL_CATEGORY + setmealDto.getCategoryId();
        stringRedisTemplate.delete(key);
        // 首先将套餐信息添加进setmeal表
        save(setmealDto);

        // 接下来将套餐的菜品信息保存进setmeal_dish表中
        // 从setmealDto中取出套餐的菜品信息
        List<SetmealDish> dishs = setmealDto.getSetmealDishes();
        // 不能直接插入表中，非空字段setmealId为空
        // 上面将套餐信息保存进表中，mybatisplus已经给setmealDto的id字段使用雪花算法赋值了
        // 使用stream流式编程为每个菜品的setmealId字段赋值
        dishs = dishs.stream()
                .map(dish -> {
                    dish.setSetmealId(setmealDto.getId());
                    return dish;
                })
                .collect(Collectors.toList());
        // 现在方可插入
        setmealDishService.saveBatch(dishs);
        return R.success("新增套餐成功");
    }

    @Override
    public R<SetmealDto> getSetmealAndDishs(Long id) {
        // 先查询出套餐信息
        Setmeal setmeal = getById(id);
        // 将套餐基本信息封装进setmealDto对象
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 再查询出套餐对应的菜品信息
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(lqw);

        // 将套餐的菜品信息也封装进setmealDto中
        setmealDto.setSetmealDishes(setmealDishes);

        return R.success(setmealDto);
    }

    @Override
    @Transactional
    public R<String> updateSetmeal(SetmealDto setmealDto) {

        String key = SETMEAL_CATEGORY + setmealDto.getCategoryId();
        stringRedisTemplate.delete(key);
        // 首先更新套餐基本信息
        updateById(setmealDto);
        // 再将套餐对应的菜品全部删除
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lqw);
        // 再将修改后的菜品全部插入()
        // 需要先将每个菜品对应的套餐id映射到setmealDish上
        List<SetmealDish> dishs = setmealDto.getSetmealDishes();

        dishs = dishs.stream()
                .map(dish -> {
                    dish.setSetmealId(setmealDto.getId());
                    return dish;
                })
                .collect(Collectors.toList());
        // 进行插入
        setmealDishService.saveBatch(dishs);

        return R.success("套餐修改成功");
    }

    @Override
    public R<String> deleteByIds(String ids) {
        // 首先解析出id，转化为Long型
        List<Long> idList = stringTransList(ids);
        // 删除套餐前需判断套餐是否处于停售状态，只有处于停售状态的套餐可删除
        for (Long id : idList) {
            Setmeal setmeal = getById(id);
            if (setmeal.getStatus() == 1) {
                return R.error("套餐不是停售状态，不可删除！");
            }
        }
        // 走到这里，说明可以删除
        // 首先删除套餐
        removeByIds(idList);
        // 在删除setmeal_dish表中对应的菜品
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        idList.forEach(id -> {
                    lqw.eq(SetmealDish::getSetmealId, id);
                    setmealDishService.remove(lqw);
                });
        return R.success("删除成功");
    }

    @Override
    public R<String> modifyStatus(Integer status, String ids) {
        // 同样解析出id，不过解析id的同时生成一个setmeal对象，将状态和id封装进setmeal对象
        List<Setmeal> list = Stream.of(ids.split(","))
                .map(id -> {
                    // 修改状态的同时要清空套餐缓存
                    String key = SETMEAL_CATEGORY + getById(id).getCategoryId();
                    stringRedisTemplate.delete(key);

                    long setmealId = Long.parseLong(id);
                    Setmeal setmeal = new Setmeal();
                    setmeal.setId(setmealId);
                    setmeal.setStatus(status);
                    return setmeal;
                })
                .collect(Collectors.toList());
        // 批量修改
        updateBatchById(list);
        return R.success("状态修改成功");
    }

    @Override
    public R<List<Setmeal>> listByUser(Setmeal setmeal) {
        // 查出起售套餐下的所有套餐
        // 查询缓存
        String key = SETMEAL_CATEGORY + setmeal.getCategoryId();
        String setmealListJSON = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(setmealListJSON)) {
            // 缓存命中，直接返回
            return R.success( JSON.parseArray(setmealListJSON, Setmeal.class));
        }
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());

        List<Setmeal> list = list(lqw);
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list));


        return R.success(list);
    }

    private List<Long> stringTransList(@NotNull String ids) {
        List<Long> idList = Stream.of(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return idList;
    }
}
