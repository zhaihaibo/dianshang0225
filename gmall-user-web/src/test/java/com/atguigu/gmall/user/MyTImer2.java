package com.atguigu.gmall.user;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimerTask;

public class MyTImer2 extends TimerTask {
    @Override
    public void run() {
        DateFormat dateFormat = TimeUtil.df.get();

        System.out.println("测试timer类2222===========>"+dateFormat.format(new Date()));
    }
}
