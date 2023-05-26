package com.dh.reggie.controller;

import com.dh.reggie.common.R;
import com.dh.reggie.entiry.ShoppingCart;
import com.dh.reggie.service.IShoppingCartService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private IShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        return shoppingCartService.add(shoppingCart);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> listShoppingCart() {
        return shoppingCartService.listShoppingCart();
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        return shoppingCartService.sub(shoppingCart);
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        return shoppingCartService.clean();
    }
}
