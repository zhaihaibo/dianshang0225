package com.atguigu;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class mq {

    public static final String ACTIVE_MQ="tcp://192.168.1.26:61616";
    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_MQ);
        Connection connection =null;
        try {
             connection = activeMQConnectionFactory.createConnection();
             connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("one");
            MessageProducer producer = session.createProducer(queue);
            for (int i = 0; i <3 ; i++) {

                TextMessage textMessage = session.createTextMessage(i+"");//消息体
                textMessage.setJMSPriority(8);//消息头
                textMessage.setStringProperty("c01","vip"); //消息属性  增加消息的去重/筛选/过滤/标志
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT); //消息的非持久化机制！
                producer.send(textMessage);


            }

        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            connection.close();
            System.out.println("消息发成功了！");
        }

    }
}
