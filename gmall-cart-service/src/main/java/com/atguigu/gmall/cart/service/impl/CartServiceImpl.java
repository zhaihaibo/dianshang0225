package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.manage.redisutils.RedisUtil;
import com.atguigu.gmall.user.bean.OmsCartItem;
import com.atguigu.gmall.user.service.CartService;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem isCartExist(OmsCartItem omsCartItem) {

        OmsCartItem omsCartItem1 = new OmsCartItem();

        omsCartItem1.setProductSkuId(omsCartItem.getProductSkuId());
        omsCartItem1.setMemberId(omsCartItem.getMemberId());
        OmsCartItem omsCartItem2 = omsCartItemMapper.selectOne(omsCartItem1);


        return omsCartItem2;
    }

    //更新
    @Override
    public void updataCartById(OmsCartItem omsCartItemExist) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setQuantity(omsCartItemExist.getQuantity());
        omsCartItem.setTotalPrice(omsCartItemExist.getPrice().multiply(omsCartItemExist.getQuantity()));

        Example example = new Example(OmsCartItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(omsCartItemExist.getId());

        omsCartItemMapper.updateByExampleSelective(omsCartItemExist, example);

        //调用同步缓存

        setCache(omsCartItemExist.getMemberId());


    }

    //新增
    @Override
    public void insertCart(OmsCartItem omsCartItem) {

        omsCartItemMapper.insert(omsCartItem);

        //调用同步缓存
        setCache(omsCartItem.getMemberId());
    }

    //显示购物车时从缓存中取数据
    @Override
    public List<OmsCartItem> getCartCache(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {

            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            for (String hval : hvals) {

                OmsCartItem omsCartItem1 = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItem1.setTotalPrice(omsCartItem1.getPrice().multiply(omsCartItem1.getQuantity()));
                omsCartItems.add(omsCartItem1);
            }

        } finally {
            jedis.close();
        }


        return omsCartItems;
    }

    @Override
    public void updataCartIsChecked(OmsCartItem omsCartItem) {

        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setIsChecked(omsCartItem.getIsChecked());

        //根据skuid和member进行更新
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("productSkuId", omsCartItem.getProductSkuId()).andEqualTo("memberId", omsCartItem.getMemberId());

        omsCartItemMapper.updateByExampleSelective(cartItem, example);

        //刷新缓存
        setCache(omsCartItem.getMemberId());
    }

    //缓存
    private void setCache(String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
            HashMap<String, String> hashMap = new HashMap<>();
            for (OmsCartItem cartItem : omsCartItems) {
                hashMap.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            String key = "user:" + memberId + ":cart";
            jedis.hmset(key, hashMap);
        } finally {
            jedis.close();
        }
    }

}
