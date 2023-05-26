package com.dh.reggie.dto;


import com.dh.reggie.entiry.OrderDetail;
import com.dh.reggie.entiry.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {
    private List<OrderDetail> orderDetails;
	
}
