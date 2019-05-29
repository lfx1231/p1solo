package cn.itcast.core.service;

import cn.itcast.core.util.HttpClient;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
@Service
public class PayServiceImpl implements PayService {
    //微信公众账号唯一标识
    @Value("${appid}")
    private String appid;

    //财富通平台的商户账号
    @Value("${partner}")
    private String partner;

    //财付通平台商户密钥
    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String tradeNo, String totalFee) {
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);//公众号
        param.put("mch_id",partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","品优购");//商品描述
        param.put("out_trade_no",tradeNo);//订单号
        param.put("total_fee",totalFee);//总金额，分
        param.put("spbill_create_ip","127.0.0.1");//ip
        param.put("notify_url","http://www.itcast.cn");//回调地址随便写
        param.put("trade_type","NATIVE");//交易类型，扫描支付？
        try {
            //生成要发送的 xml格式数据
            String xmlParam = WXPayUtil.generateSignedXml(param,partnerkey);
            System.out.println(xmlParam);
            //调用微信支付统一下单接口，通过httpClient工具发送请求。
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置协议为https
            client.setHttps(true);
            //发送xml格式的数据
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            //调用微信工具类将返回的xml格式字符串转换成map格式
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            //获取我们需要发给前端的 支付地址链接，总金额和订单号，并封装到新的map中，最后放回return
            Map<String,String> map = new HashMap<>();
            map.put("code_url",resultMap.get("code_url"));//支付链接
            map.put("total_fee",totalFee);//金额
            map.put("out_trade_no",tradeNo);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map queryPayStatus(String tradeNo) {
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);//公众号
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",tradeNo);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            System.out.println(resultMap);
            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
