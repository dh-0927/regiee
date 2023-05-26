package com.dh.reggie.dto;


import com.dh.reggie.entiry.Setmeal;
import com.dh.reggie.entiry.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
