package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

import java.util.List;

public interface OrderService {
    void add(Order order,List<BuyerCart> cartList);

    PayLog getPayLogFormRedis(String username);

    void updateOrderStatus(String out_trade_no, String transaction_id);
}
