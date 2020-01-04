package com.atguigu.gmall.user;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MyTimer1 extends TimerTask {
    @Override
    public void run() {
        //每天0点要做的事


        try {
            DateFormat dateFormat = TimeUtil.df.get();
            Thread.sleep(1000);
            System.out.println("测试timer类1111===========>"+dateFormat.format(new Date()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
