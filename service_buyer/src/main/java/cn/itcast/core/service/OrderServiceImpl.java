package cn.itcast.core.service;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.Constants;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private PayLogDao payLogDao;
    @Autowired
    private OrderItemDao orderItemDao;


    @Override
    public void add(Order order,List<BuyerCart> cartList) {
        //1.遍历集合，每一个BuyerCart对象，就是一个order订单（属于某个卖家的）,
        // 封装该order，并存入数据库的order表中
        List<String> orderIdList = new ArrayList<>();
        long totalMoney = 0;
        for (BuyerCart buyerCart : cartList) {
            Order pygorder = new Order();
            long orderId = idWorker.nextId();
            System.out.println(orderId);
            pygorder.setOrderId(orderId);
            orderIdList.add(String.valueOf(pygorder.getOrderId()));
            pygorder.setStatus("1");


            pygorder.setSellerId(buyerCart.getSellerId());
            pygorder.setPaymentType(order.getPaymentType());
            pygorder.setCreateTime(new Date());
            pygorder.setUpdateTime(new Date());
            pygorder.setUserId(order.getUserId());
            pygorder.setReceiver(order.getReceiver());
            pygorder.setReceiverMobile(order.getReceiverMobile());
            pygorder.setReceiverAreaName(order.getReceiverAreaName());
            pygorder.setSourceType(order.getSourceType());//订单来源

            //2.再遍历BuyerCart对象里的List<OrderItem>，封装orderItem对象，
            // 存入数据库orderItem表中，并计算总金额
            double money = 0;
            for (OrderItem orderItem : buyerCart.getOrderItemList()) {
                OrderItem porderItem = new OrderItem();
                porderItem.setItemId(orderItem.getItemId());
                porderItem.setId(idWorker.nextId());//重要，不能没有
                porderItem.setOrderId(pygorder.getOrderId());
                porderItem.setGoodsId(orderItem.getGoodsId());
                porderItem.setSellerId(buyerCart.getSellerId());
                porderItem.setTitle(orderItem.getTitle());
                porderItem.setPrice(orderItem.getPrice());
                porderItem.setNum(orderItem.getNum());
                porderItem.setPicPath(orderItem.getPicPath());
                porderItem.setTotalFee(porderItem.getPrice().multiply(new BigDecimal(porderItem.getNum())));
                orderItemDao.insertSelective(porderItem);
                 money += porderItem.getTotalFee().doubleValue();
            }
            pygorder.setPayment(new BigDecimal(money));
            orderDao.insertSelective(pygorder);
            totalMoney+=money; //累计相加 得订单的总金额。

        }

        //3.判断支付类型是否为微信支付，也就是1，2为货到付款，封装payLog对象，
        // 存入数据库。把payLog对象以hash的形式，保存到redis中
            if("1".equals(order.getPaymentType())){
            //说明是微信支付
                PayLog payLog = new PayLog();
                payLog.setCreateTime(new Date());
                String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
                payLog.setOrderList(ids);
                payLog.setTotalFee(totalMoney*100);
                payLog.setUserId(order.getUserId());
                payLog.setPayType("1");
                payLog.setTradeState("0");
                String tradeNo = String.valueOf(idWorker.nextId());
                payLog.setOutTradeNo(tradeNo);
                payLogDao.insertSelective(payLog);
                redisTemplate.boundHashOps(Constants.REDIS_PAYLOG).put(order.getUserId(),payLog);

            }
        //4.删除redis中的购物车。
        redisTemplate.boundHashOps(Constants.REDIS_CARTLIST).delete(order.getUserId());
    }

    @Override
    public PayLog getPayLogFormRedis(String username) {
        PayLog payLog =(PayLog) redisTemplate.boundHashOps(Constants.REDIS_PAYLOG).get(username);
        //需要注意 如果没有容易导致 空指针。
       if(payLog == null){
           return new PayLog();
        }
        return payLog;
    }

    @Override
    public void updateOrderStatus(String tradeNo, String transaction_id) {
        //根据 主键tradeNo 对外订单号查一个payLog对象
        PayLog payLog = payLogDao.selectByPrimaryKey(tradeNo);
        payLog.setTransactionId(transaction_id);
        payLog.setTradeState("1");//未支付是0，支付了是1
        payLog.setPayTime(new Date());
        payLogDao.updateByPrimaryKeySelective(payLog);
        // 修改 order表的状态
        String[] orderids = payLog.getOrderList().split(",");//获取 订单号数组
        if(orderids!=null){
            for (String orderid : orderids) {
                Order order = new Order();
                order.setStatus("2");//1为未支付，2未已支付
                order.setOrderId(Long.parseLong(orderid));
                orderDao.updateByPrimaryKeySelective(order);
            }
        }
        //清除redis中 未支付的payLog 对象数据
        redisTemplate.boundHashOps(Constants.REDIS_PAYLOG).delete(payLog.getUserId());

    }


}
