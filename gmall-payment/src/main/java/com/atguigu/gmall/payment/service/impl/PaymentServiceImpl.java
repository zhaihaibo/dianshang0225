package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.manage.redisutils.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.user.bean.PaymentInfo;
import com.atguigu.gmall.user.service.PaymentService;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void addPayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePaymentByOrderSn(PaymentInfo paymentInfo) {

        Example example = new Example(PaymentInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderSn",paymentInfo.getOrderSn());


        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }



    @Override
    public void sendPaymentSuccess(PaymentInfo paymentInfo) {

        ConnectionFactory factory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        try {
            connection = factory.createConnection();
            Session session = connection.createSession(true,Session.SESSION_TRANSACTED);
            //创建队列
            Queue queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            //创建消息的生产者
            MessageProducer producer = session.createProducer(queue);
           //发送信息 第一种map，第二种文本！

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            mapMessage.setString("pay_amount",paymentInfo.getTotalAmount().toString());
            mapMessage.setString("status","success");
            producer.send(mapMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public void sendPaymentStatusCheckQueue(PaymentInfo paymentInfo, int i) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);

            //信息
            ActiveMQMapMessage message = new ActiveMQMapMessage();
            message.setString("out_trade_no",paymentInfo.getOrderSn());
            //此处的i记录的是重复访问发消息的次数！
            message.setInt("conut",i);
            //设置延迟触发 5秒
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,5*1000);
            //发信息！
            producer.send(message);
            session.commit();
        } catch  (JMSException e) {
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

    }

    //从支付宝回调中获取支付信息
    @Override
    public Map<String,Object> checkPaymentStatus(String out_trade_no) {
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_trade_no);

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //业务参数out_trade_no 和trade_no	选一个即可！
        returnMap.put("out_trade_no",out_trade_no);

        String requestMapJSON = JSON.toJSONString(returnMap);
        request.setBizContent(requestMapJSON);


        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("交易已创建,调用成功");
            String tradeStatus = response.getTradeStatus();
            returnMap.put("trade_status",tradeStatus);
            returnMap.put("out_trade_no",out_trade_no);
            returnMap.put("trade_no",response.getTradeNo());
            String responseJSON = JSON.toJSONString(response);
            returnMap.put("callbackContent",responseJSON);
        } else {
            // 如果用户没有登录支付宝，状态是交易未创建
            returnMap.put("trade_status","");
            returnMap.put("out_trade_no",out_trade_no);
            System.out.println("交易未创建，调用失败");
        }

        return returnMap;
    }

    @Override
    public String checkDbPayStatus(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_trade_no);

        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);
        if (StringUtils.isNotBlank(paymentInfo1.getPaymentStatus())&&paymentInfo1.getPaymentStatus().equals("已支付")){
            return "success";
        }else {
            return  "fail";
        }
    }
}
