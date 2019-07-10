package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.manage.redisutils.RedisUtil;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySkuId(String spuId, String skuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsSkuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySkuId(spuId, skuId);
        return pmsProductSaleAttrs;

    }

    @Override
    public List<PmsSkuInfo> checkSkuBySpuId(String spuId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuSaleAttrValueMapper.checkSkuBySpuId(spuId);

        return pmsSkuInfos;
    }

    //获取skuid的方法二
    @Override
    public String checkSkuByValueIdsTwo(String[] ids) {
        String skuid1 = null;
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuSaleAttrValueMapper.checkSkuByValueIdsTwo(StringUtils.join(ids, ","));
        HashMap<String, String> hashMap = new HashMap<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuid = pmsSkuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
            String skuHash = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValues) {

                //找SaleAttrValueId
                skuHash = skuHash + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();
            }
            hashMap.put(skuHash, skuid);

        }
        String valueIds = "";
        for (String id : ids) {
            valueIds = valueIds + "|" + id;
        }
        String s = hashMap.get(valueIds);
        if (s != null && !s.equals("")) {
            skuid1 = s;
        }

        return skuid1;
    }


    //获取skuid的方法一
    @Override
    public String checkSkuByValueIds(String[] ids) {

        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuSaleAttrValueMapper.checkSkuByValueIds(StringUtils.join(ids, ","));
        String skuId = null;
        List<String> skuids = new ArrayList<>();
        if (pmsSkuSaleAttrValues != null && pmsSkuSaleAttrValues.size() > 0) {

            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
                skuids.add(pmsSkuSaleAttrValue.getSkuId());
            }
        }
        HashMap<String, String> hashMap = new HashMap<>();

        for (String skuidFromList : skuids) {
            String skuHash = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
                String skuIdFromDB = pmsSkuSaleAttrValue.getSkuId();
                if (skuidFromList.equals(skuIdFromDB)) {
                    //找SaleAttrValueId
                    skuHash = skuHash + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();
                }
            }
            //生成hash表 ，每一个skuid都有相对应的多个SaleAttrValueId
            hashMap.put(skuHash, skuidFromList);
        }

        //生成形参中的ids的hash表数值！
        String valueIds = "";
        for (String id : ids) {
            valueIds = valueIds + "|" + id;
        }

        String skuid = hashMap.get(valueIds);
        if (skuid != null && !skuid.equals("")) {
            skuId = skuid;
        }

        return skuId;
    }


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        String spuId = pmsSkuInfo.getSpuId();

        pmsSkuInfo.setProductId(spuId);

        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String id = pmsSkuInfo.getId();
        //插入图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(id);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
        //保存平台属性
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {

            pmsSkuAttrValue.setSkuId(id);
            skuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        // 保存销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(id);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }


    }

    @Override
    public PmsSkuInfo getSkuInfoByIdFromDB(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo1.setSkuImageList(pmsSkuImages);
        System.out.println(pmsSkuInfo1);


        return pmsSkuInfo1;
    }

    @Override
    public PmsSkuInfo getSkuInfoById(String skuId, String ip) {

        System.out.println("ip" + ip + Thread.currentThread().getName() + "进入商品：" + skuId + "的请求");
        //查询商品缓存
        Jedis jedis = redisUtil.getJedis();
        String skuKey = "sku:" + skuId + ":info";
        String lockKey = "sku:" + skuId + ":lock";
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        try {
            //获取redis中key的value
            String key = jedis.get(skuKey);
            if (StringUtils.isBlank(key)) {
                System.out.println("ip" + ip + Thread.currentThread().getName() + "在缓存中没有找到：" + skuId + "的请求，准备请求数据库");
                //利用redis的nx特性：如果存在不允许修改！ 可以初步保证线程的安全问题
                String uuid = UUID.randomUUID().toString();
                String OK = jedis.set(lockKey, uuid, "nx", "px", 3000);//添加分布式锁
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                    System.out.println("ip:" + ip + Thread.currentThread().getName() + "获得:" + skuId + "分布式锁，开始请求mysql");
                    Thread.sleep(3000);
                    //如果缓存不存在，查找mysql
                    pmsSkuInfo = getSkuInfoByIdFromDB(skuId);
                    //查询结果返回给用户，并同步redis缓存
                    if (pmsSkuInfo != null) {
                        jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                    }
                    System.out.println("ip:" + ip + Thread.currentThread().getName() + "请求mysql成功，归还" + skuId + "的分布式锁");
                    //删除分布式锁时 ，判断删除的是否是之前自己生成的uuid ，防止误删别人的
                    String v = jedis.get(lockKey);
                    if (StringUtils.isNotBlank(v) && v.equals(uuid)) {
                        jedis.del(lockKey);
                    }
                } else {
                    System.out.println("ip:" + ip + Thread.currentThread().getName() + "没有获得:" + skuId + "分布式锁，开始自旋。。。。。");
                    Thread.sleep(7000);
                    //如果别的线程进来，因为nx，不能修改lockKey，返回值不是OK，进不去上边的方法，开始自旋
                    return getSkuInfoById(skuId, ip);
                }


            } else {
                System.out.println("ip:" + ip + Thread.currentThread().getName() + "从缓存中获取商品:" + skuId + "的数据");
                //如果存在直接返回
                pmsSkuInfo = JSON.parseObject(key, PmsSkuInfo.class);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }


        //关闭redis资源

        System.out.println("ip:" + ip + Thread.currentThread().getName() + "请求结束");
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> select = skuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }
}
