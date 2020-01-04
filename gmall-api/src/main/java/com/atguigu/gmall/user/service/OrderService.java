package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.OmsOrder;


public interface OrderService {


    void addOrder(OmsOrder omsOrder);

    String genTradeCode(String memberId);

    boolean checkOrderCode(String memberId, String tradeCode);

    OmsOrder getOrderByOrderSn(String orderSn);

    void updateOrder(OmsOrder omsOrder);

    void sendOrderPayQueue(String out_trade_no);
}
