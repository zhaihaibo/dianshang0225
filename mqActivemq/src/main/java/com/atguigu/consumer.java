package com.atguigu;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;


public class consumer {
    public static final String ACTIVE_MQ="tcp://192.168.1.26:61616";
    public static void main(String[] args) throws Exception {

            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_MQ);
           Connection connection = activeMQConnectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("one");
        //创建消费者
        MessageConsumer consumer = session.createConsumer(queue);
/*
        同步阻塞方式 使用receive（）
        while (true){
            //receive代表消费者一直等着
            TextMessage message = (TextMessage)consumer.receive();
            if (message!=null){
                System.out.println("接受到了消息："+message.getText());
            }else{
                break;
            }
        }
        consumer.close();
        session.close();
        connection.close();
*/
        //通过监听的方式来消费消息！ 异步非阻塞的方式 onMessage（）；
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message!=null&&message instanceof TextMessage){
                    TextMessage message1 = (TextMessage) message;
                    try {
                        System.out.println("接受到了消息--"+message1.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        System.in.read();
        consumer.close();
        session.close();
        connection.close();

    }
}
