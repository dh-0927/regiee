package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.BaseContext;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.ShoppingCart;
import com.dh.reggie.mapper.ShoppingCartMapper;
import com.dh.reggie.service.IShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    @Override
    public R<ShoppingCart> add(ShoppingCart shoppingCart) {
        // 设置用户id，指定是哪个用户的购物车id
        Long userId = BaseContext.get();
        shoppingCart.setUserId(userId);
        // 查询添加的菜品或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId)
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart shopCart = getOne(lqw);
        // 如果已经存在，数量加一
        if (shopCart != null) {
            shopCart.setNumber(shopCart.getNumber() + 1);
            updateById(shopCart);
            return R.success(shopCart);
        }
        // 如果不存在，则添加到购物车，数量默认就是1
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        if (save(shoppingCart)) {
            return R.success(shoppingCart);
        }
        return R.error("添加购物车失败！");

    }

    @Override
    public R<List<ShoppingCart>> listShoppingCart() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.get())
                .orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = list(lqw);

        return R.success(list);
    }

    @Override
    public R<ShoppingCart> sub(ShoppingCart shoppingCart) {

        // 先查询出当前用户当前菜品或套餐详情
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.get())
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart shop = getOne(lqw);
        if (shop == null) {
            return R.error("操作失败！");
        }

        // 判断直接删除还是数量减一
        int number = shop.getNumber() - 1;
        if (number < 1) {
            //直接删除
            remove(lqw);
            shop.setNumber(0);
            return R.success(shop);
        }
        // 更新number
        shop.setNumber(number);
        if (updateById(shop)) {
            return R.success(shop);
        }
        return R.error("操作失败！");
    }

    @Override
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.get());

        if (remove(lqw)) {
            return R.success("删除成功");
        }
        return R.error("删除失败！");
    }


}
