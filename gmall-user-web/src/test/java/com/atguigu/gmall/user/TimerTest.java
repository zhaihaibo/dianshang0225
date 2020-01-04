package com.atguigu.gmall.user;

import java.text.ParseException;
import java.util.Timer;

public class TimerTest {
    private  static Timer timer = new Timer();
    public static void main(String[] args) throws ParseException {
        timer.schedule(new MyTimer1(),TimeUtil.df.get().parse("2017-09-14 09:19:30"));

        timer.schedule(new MyTImer2(),TimeUtil.df.get().parse("2017-09-14 09:19:30"));

    }
}
