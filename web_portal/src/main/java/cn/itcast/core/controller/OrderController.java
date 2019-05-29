package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.BuyerCartService;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/order")
@RestController
public class OrderController {
    @Reference
    private OrderService orderService;
    @Reference
    private BuyerCartService buyerCartService;


    @RequestMapping("/add")
    public Result add(@RequestBody Order order){
        //获取当前用户信息 用户名
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //根据用户名获取 redis中保存的购物车信息
            List<BuyerCart> cartList = buyerCartService.getCartListByRedis(username);
            //把当前的用户信息，设置到order当中
            order.setUserId(username);
            //根据购物车列表和 传递来的order对象 封装 添加order到数据库。
            orderService.add(order,cartList);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }

}
