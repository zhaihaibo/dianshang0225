package com.atguigu.gmall.manage.controller;

import com.atguigu.gmall.manage.redisutils.RedisUtil;


import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class TestRedission {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("testItem")
    @ResponseBody
    public String testItem(){
        Jedis jedis = redisUtil.getJedis();// redis链接
        RLock lock = redissonClient.getLock("sku:111:lock");
        String v = "";
        lock.lock();
        try {
            v = jedis.get("k");//获取value
            if(StringUtils.isBlank(v)){
                v = "1";
            }
            System.err.println("==>"+v);//打印value
            long inum = Long.parseLong(v);//获得value的值
            jedis.set("k", inum+1+"");//value增加1

        } finally {
            lock.unlock();
            jedis.close();
        }

        return v;
    }

}
