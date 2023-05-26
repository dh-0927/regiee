package com.dh.reggie.dto;


import com.dh.reggie.entiry.Dish;
import com.dh.reggie.entiry.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

//
//    private Integer copies;
}
