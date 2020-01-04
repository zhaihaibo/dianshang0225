package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.manage.redisutils.ActiveMQUtil;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.manage.redisutils.RedisUtil;
import com.atguigu.gmall.user.bean.OmsOrder;
import com.atguigu.gmall.user.bean.OmsOrderItem;
import com.atguigu.gmall.user.service.OrderService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;



    @Override
    public void addOrder(OmsOrder omsOrder) {

        // 添加订单，生成订单号
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        // 根据订单号，添加订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();

        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
    }

    //生成交易码放入redis
    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCode=null;
        try {

            tradeCode = UUID.randomUUID().toString();
            String key = "user:"+memberId+":tradeCode";
            jedis.setex(key,60*30,tradeCode);
        }finally {
            jedis.close();
        }


        return tradeCode;
    }

    //提交订单时检查交易码
    @Override
    public boolean checkOrderCode(String memberId, String tradeCode) {
        boolean b =false;
        Jedis jedis = redisUtil.getJedis();
        try {
            String key = "user:"+memberId+":tradeCode";
            String tradeCodeFromRedis = jedis.get(key);
            if (tradeCodeFromRedis.equals(tradeCode)){
                b = true;
                jedis.del(key);
            }else {
                b = false;
            }
        }finally {
            jedis.close();
        }
        return b;
    }

    @Override
    public OmsOrder getOrderByOrderSn(String orderSn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);

        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderId(omsOrder1.getId());
        List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);
        omsOrder1.setOmsOrderItems(select);
        return omsOrder1;
    }

    //接受到支付成功后，根据orderSn再次更新订单
    @Override
    public void updateOrder(OmsOrder omsOrder) {

        Example example = new Example(OmsOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderSn",omsOrder.getOrderSn());

        omsOrderMapper.updateByExampleSelective(omsOrder,example);

    }

    @Override
    public void sendOrderPayQueue(String out_trade_no) {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);// 事务型消息

            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");

            MessageProducer producer = session.createProducer(payment_success_queue);

            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(out_trade_no);
            OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderId(omsOrderItem.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);
            omsOrder1.setOmsOrderItems(select);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(JSON.toJSONString(omsOrder));
            producer.send(textMessage);

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
}
