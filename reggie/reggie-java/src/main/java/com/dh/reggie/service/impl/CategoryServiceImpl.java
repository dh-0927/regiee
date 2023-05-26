package com.dh.reggie.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.Category;
import com.dh.reggie.entiry.Dish;
import com.dh.reggie.entiry.Setmeal;
import com.dh.reggie.entiry.SetmealDish;
import com.dh.reggie.mapper.CategoryMapper;
import com.dh.reggie.mapper.DishMapper;
import com.dh.reggie.mapper.SetmealMapper;
import com.dh.reggie.service.ICategoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.dh.reggie.constants.BackendConstants.USER_CATEGORY;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public R<IPage> pageList(Integer page, Integer pageSize) {
        IPage<Category> pageList = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper();
        lqw.orderByAsc(Category::getSort);
        page(pageList, lqw);
        return R.success(pageList);
    }

    @Override
    public R<String> addCategory(Category category) {
        // 判断该菜名是否存在
        Category one = getOne(new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName()));
        if (one != null) {
            return R.error("分类已存在！");
        }
        // 如果不存在，添加菜品
        if (save(category)) {
            stringRedisTemplate.delete(USER_CATEGORY);
            return R.success("分类添加成功");
        }
        return R.error("添加失败！");
    }

    @Override
    public R<String> update(Category category) {
        stringRedisTemplate.delete(USER_CATEGORY);
        if (updateById(category)) {
            return R.success("修改成功");
        }
        return R.error("修改失败！");

    }

    @Override
    public R<String> delete(Long id) {
        // 判断是套餐还是菜品
        Integer type = getById(id).getType();
        if (type == 1) {
            LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
            // 如果是菜品，去dish表中查找
            lqw.eq(Dish::getCategoryId, id);
            List<Dish> dishes = dishMapper.selectList(lqw);
            if (dishes.size() != 0) {
                // 该菜品下有菜，不能删除，返回错误信息
                return R.error("该菜品不能删除！");
            }

        } else {
            // 如果是套餐，去setmeal中查找
            LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
            // 如果是菜品，去dish表中查找
            lqw.eq(Setmeal::getCategoryId, id);
            List<Setmeal> setmeals = setmealMapper.selectList(lqw);
            if (setmeals.size() != 0) {
                // 该套餐下有套餐，不能删除，返回错误信息
                return R.error("该套餐不能删除！");
            }

        }
        // 否则，可以删除
        if (removeById(id)) {
            stringRedisTemplate.delete(USER_CATEGORY);
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    @Override
    public R<List<Category>> list(Integer type) {

                if (type == null) {

                    String categoryJSON = stringRedisTemplate.opsForValue().get(USER_CATEGORY);
                    if (StringUtils.isNotBlank(categoryJSON)) {
                        return R.success(JSON.parseArray(categoryJSON, Category.class));
                    }

                    List<Category> categoryList = list().stream()
                    .filter(category -> {
                        // 不展示类别下没有菜品的
                        if (category.getType() == 1) {
                            // dish表中查找
                            LambdaQueryWrapper<Dish> lqw1 = new LambdaQueryWrapper<>();
                            lqw1.eq(Dish::getCategoryId, category.getId());
                            return dishMapper.selectCount(lqw1) > 0;
                        } else {
                            // 套餐表中查找
                            LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
                            lqw2.eq(Setmeal::getCategoryId, category.getId());
                            return setmealMapper.selectCount(lqw2) > 0;
                        }
                    }).collect(Collectors.toList());

            stringRedisTemplate.opsForValue().set(USER_CATEGORY, JSON.toJSONString(categoryList));

            return R.success(categoryList);
        }

        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();

        lqw.eq(Category::getType, type);
        List<Category> list = list(lqw);
        return R.success(list);
    }
}
