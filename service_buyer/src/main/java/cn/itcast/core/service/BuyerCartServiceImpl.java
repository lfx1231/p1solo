package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BuyerCartServiceImpl implements BuyerCartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemDao itemDao;


    @Override
    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num) {
        Item item = itemDao.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("您购买的商品不存在!");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("您购买的商品未经审核，非法！");
        }
        String sellerId = item.getSellerId();
        BuyerCart buyerCart = findBuyerCart(cartList, sellerId);
        if(buyerCart == null){
            buyerCart = new BuyerCart();
            buyerCart.setSellerId(sellerId);
            buyerCart.setSellerName(item.getSeller());
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = createNewOrderItem(item,num);
            orderItemList.add(orderItem);
            buyerCart.setOrderItemList(orderItemList);
            cartList.add(buyerCart);
        }else{
            List<OrderItem> orderItemList = buyerCart.getOrderItemList();
            OrderItem orderItem = findOrderItem(orderItemList,itemId);
            if(orderItem == null){
               orderItem = createNewOrderItem(item,num);
                orderItemList.add(orderItem);
            }else{
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
                if(orderItem.getNum()<=0){
                    orderItemList.remove(orderItem);
                }
                if(orderItemList.size()==0){
                    cartList.remove(buyerCart);
                }
            }

        }

    return cartList;

    }

    @Override
    public List<BuyerCart> getCartListByRedis(String username) {
        List<BuyerCart> cartList =(List<BuyerCart>) redisTemplate.boundHashOps(Constants.REDIS_CARTLIST).get(username);
        return cartList;
    }

    public OrderItem findOrderItem(List<OrderItem> orderItemList,Long itemId){
        if(orderItemList!=null && orderItemList.size()>0){
            for (OrderItem orderItem : orderItemList) {
               if(itemId.equals(orderItem.getItemId())) {
                   return orderItem;
               }
            }
        }
        return null;
    }
    public OrderItem createNewOrderItem(Item item,Integer num){
        if(num==null || num <= 0){
            throw new RuntimeException("您不能添加0个商品到购物车");
        }
        OrderItem orderItem = new OrderItem();

        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setItemId(item.getId());
        orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(num)));

        return orderItem;
    }


    public BuyerCart findBuyerCart(List<BuyerCart> cartList,String sellerId){
        if (cartList!=null){
            for (BuyerCart cart : cartList) {
                if(sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }


    @Override
    public List<BuyerCart> mergeCookieRedis(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList) {
        if(cookieCartList!=null ){
            for (BuyerCart cart : cookieCartList) {
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList!=null && orderItemList.size()>0){
                    for (OrderItem orderItem : orderItemList) {
                        Long itemId = orderItem.getItemId();
                        Integer num = orderItem.getNum();
                       redisCartList = addItemToCartList(redisCartList, itemId, num);
                    }
                }
            }
        }
        return redisCartList;
    }

    @Override
    public void setCartListToRedis(String username, List<BuyerCart> redisCartList) {
        redisTemplate.boundHashOps(Constants.REDIS_CARTLIST).put(username,redisCartList);
    }


}
