package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PaymentInfo;

import java.util.Map;


public interface PaymentService {
    void addPayment(PaymentInfo paymentInfo);

    void updatePaymentByOrderSn(PaymentInfo paymentInfo);



    void sendPaymentSuccess(PaymentInfo paymentInfo);

    void sendPaymentStatusCheckQueue(PaymentInfo paymentInfo, int i);

    Map<String,Object> checkPaymentStatus(String out_trade_no);

    String checkDbPayStatus(String out_trade_no);
}
