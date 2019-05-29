package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private PayService payService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据用户名从redis中获取 payLog 集合
        PayLog payLog = orderService.getPayLogFormRedis(username);
        //微信支付需要 一个out订单号，还要一个总金额。
        Long totalFee = payLog.getTotalFee();
        String outTradeNo = payLog.getOutTradeNo();
        //为了支付方便，这里直接把总金额写成1
        Map resultMap = payService.createNative(outTradeNo,"1");
        return resultMap;
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        int flag = 0;
        while (true){
          Map resultMap =  payService.queryPayStatus(out_trade_no);

          if(resultMap == null){
             result = new Result(false,"二维码超时");
              break;
          }
          if("SUCCESS".equals(resultMap.get("trade_state"))){
              orderService.updateOrderStatus(out_trade_no,resultMap.get("transaction_id")+"");
              result = new Result(true,"支付成功！");
              break;
          }
            if(flag>=400){
                result = new Result(false,"二维码超时");
                break;
            }
            try {
                Thread.sleep(3000);
                flag++;

            } catch (Exception e) {
                e.printStackTrace();
                result = new Result(false,"二维码超时");
            }

        }
        return result;
    }
}
