package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;

import java.util.List;

public interface BuyerCartService {
    List<BuyerCart> mergeCookieRedis(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList);

    void setCartListToRedis(String username, List<BuyerCart> redisCartList);

    List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num);

    List<BuyerCart> getCartListByRedis(String username);
}
