package com.atguigu.gmall.order.listener;


import com.atguigu.gmall.user.bean.OmsOrder;
import com.atguigu.gmall.user.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.math.BigDecimal;
import java.util.Date;

@Component
public class OrderMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentSuccess(MapMessage mapMessage) throws JMSException {

        System.out.println("订单系统消费支付信息");

        // 获得支付信息
        String out_trade_no = mapMessage.getString("out_trade_no");
        BigDecimal pay_amount = new BigDecimal(mapMessage.getDouble("pay_amount"));

        // 更新订单数据
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setStatus("1");
        omsOrder.setOrderSn(out_trade_no);
        omsOrder.setPayAmount(pay_amount);
        omsOrder.setPaymentTime(new Date());
        orderService.updateOrder(omsOrder);

        // 通知库存，锁定商品，ORDER_PAY_QUEUE
        orderService.sendOrderPayQueue(out_trade_no);

    }

}
