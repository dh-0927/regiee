package com.dh.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.ShoppingCart;

import java.util.List;

public interface IShoppingCartService extends IService<ShoppingCart> {
    R<ShoppingCart> add(ShoppingCart shoppingCart);

    R<List<ShoppingCart>> listShoppingCart();

    R<ShoppingCart> sub(ShoppingCart shoppingCart);

    R<String> clean();
}
