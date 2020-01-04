package com.atguigu.gmall.user;

import jdk.management.resource.internal.inst.SocketOutputStreamRMHooks;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class aa {


    @Test
    public void test1() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("zhangsan","张三");
        jsonObject.put("lisi","李四");
        jsonObject.put("wangwu","王五");


        String string = jsonObject.getString("wangwu");
        System.out.println(string);

        System.out.println(jsonObject);

        String s2 = jsonObject.toString();
        System.out.println(s2);

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.putOpt("u1",new User("A","啊"));
        jsonObject1.putOpt("u2",new User("B","逼"));

        System.out.println(jsonObject1);
        System.out.println(jsonObject1.getString("u1"));
        System.out.println(jsonObject1.isNull("u1"));
    }

    @Test
    public void test2() throws ParseException {

        Timer timer = new Timer();
        System.out.println(timer);
//        Calendar instance = Calendar.getInstance();
//        instance.add(Calendar.DATE,1);
//        instance.set(instance.get(Calendar.YEAR),instance.get(Calendar.MONTH),instance.get(Calendar.DATE),0,0,0);
//        long time = 1000;
        timer.schedule(new MyTimer1(),TimeUtil.df.get().parse("2017-09-14 09:19:30"));

        timer.schedule(new MyTImer2(),TimeUtil.df.get().parse("2017-09-14 09:19:30"));

    }
}
