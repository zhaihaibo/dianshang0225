package com.atguigu.gmall.user;




import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeUtil
{
    public static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
}
