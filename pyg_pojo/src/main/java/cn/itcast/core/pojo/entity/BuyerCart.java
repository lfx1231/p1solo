package cn.itcast.core.pojo.entity;

import cn.itcast.core.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;

public class BuyerCart implements Serializable {

    private String sellerId;
    private String sellerName;
    //购物项集合，也叫做订单详情集合，购物明细集合等
    private List<OrderItem> orderItemList;

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
