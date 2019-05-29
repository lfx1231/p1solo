package cn.itcast.core.service;

import java.util.Map;

public interface PayService {
    Map createNative(String tradeNo, String totalFee);

    Map queryPayStatus(String out_trade_no);
}
