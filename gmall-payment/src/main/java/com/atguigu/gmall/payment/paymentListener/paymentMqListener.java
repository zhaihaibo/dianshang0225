package com.atguigu.gmall.payment.paymentListener;


import com.atguigu.gmall.user.bean.PaymentInfo;

import com.atguigu.gmall.user.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;


import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Component
public class paymentMqListener {
    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAYMENT_CHECK_QUEUE")
    public void consumePaymentCheck(MapMessage mapMessage) throws JMSException {
        // 获得meggage中的信息！
        String out_trade_no = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("conut");
        System.out.println("开始检查订单" + out_trade_no + "的支付情况");
        //根据订单号获取订单的支付状态
        Map<String, Object> stringObjectMap = paymentService.checkPaymentStatus(out_trade_no);
        count--;
        String trade_status = (String) stringObjectMap.get("trade_status");
        if (StringUtils.isNotBlank(trade_status)) {
            //订单已创建
            if (trade_status.equals("WAIT_BUYER_PAY")) {
                //订单未支付
                if (count > 0) {
                    System.out.println("订单已经创建，但未支付");
                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setOrderSn(out_trade_no);
                    paymentService.sendPaymentStatusCheckQueue(paymentInfo, count);
                } else {
                    System.out.println("发送次数耗尽，任务结束！");
                }

            } else {
                //订单已支付！更新支付信息！发送支付队列！
                //1.进行幂等性检查！
                String payStatus = paymentService.checkDbPayStatus(out_trade_no);

                if (!payStatus.equals("success")) {
                    //2.更新支付信息
                    PaymentInfo paymentInfo = new PaymentInfo();
                    if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
                        paymentInfo.setPaymentStatus("已支付");
                    } else {
                        paymentInfo.setPaymentStatus("支付存在问题！");
                    }
                    String trade_no = (String) stringObjectMap.get("trade_no");
                    paymentInfo.setAlipayTradeNo(trade_no);
                    String callbackContent = (String) stringObjectMap.get("callbackContent");
                    paymentInfo.setCallbackContent(callbackContent);
                    paymentInfo.setCallbackTime(new Date());
                    paymentInfo.setOrderSn(out_trade_no);

                    //3.发送支付队列
                    paymentService.sendPaymentSuccess(paymentInfo);
                    //更新支付信息！
                    paymentService.updatePaymentByOrderSn(paymentInfo);

                }
            }

        } else {
            //订单未创建,一直法消息队列
            System.out.println("订单未创建");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentService.sendPaymentStatusCheckQueue(paymentInfo, count);

        }

    }

}
