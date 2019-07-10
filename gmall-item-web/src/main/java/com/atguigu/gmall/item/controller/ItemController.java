package com.atguigu.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.user.bean.PmsProductSaleAttr;
import com.atguigu.gmall.user.bean.PmsSkuInfo;
import com.atguigu.gmall.user.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.user.bean.UmsMember;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.user.service.SpuService;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuid}.html")
    public String item(@PathVariable String skuid, ModelMap model, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        //查询到skuinfo信息，然后放进model，让界面显示 ,查询sku信息
        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(skuid, ip);
        model.put("skuInfo", pmsSkuInfo);

        //查询sku对应的spu信息，销售属性和属性值,用户输入skuid时直接显示其sku信息
        List<PmsProductSaleAttr> saleAttrs = skuService.spuSaleAttrListCheckBySkuId(pmsSkuInfo.getProductId(), skuid);
        model.put("spuSaleAttrListCheckBySku", saleAttrs);


        // 隐藏的当前sku所在的spu下的销售属性值组合对应skuId的hash表，用户点击后，找到对应的skuId用
        List<PmsSkuInfo> PmsSkuInfos = skuService.checkSkuBySpuId(pmsSkuInfo.getProductId());
        // 用销售属性值的组合当作key，用skuId当作value制作一个hash表
        HashMap<String, String> skuSaleAttrMap = new HashMap<String, String>();
        for (PmsSkuInfo skuInfo : PmsSkuInfos) {
            String skuIdForHashMap = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            String valueIds = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                valueIds = valueIds + "|" + pmsSkuSaleAttrValue.getSaleAttrValueId();

            }
            //在隐藏域中做成hash表
            skuSaleAttrMap.put(valueIds, skuIdForHashMap);
        }
        //转换成json字符串 ，放进隐藏域
        model.put("skuSaleAttrMap", JSON.toJSONString(skuSaleAttrMap));
        model.put("currentSkuId", skuid);


        return "item";

    }


    @RequestMapping("test.html")
    public String test(@PathVariable String skuid, ModelMap model) {
        //测试复选框的选中状态
        model.put("check", "1");

        //测试调用js
        model.put("num", "1000");

        //测试判断
        model.put("hehe", "1");

        //测试循环2
        List<UmsMember> list1 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UmsMember umsMember = new UmsMember();
            umsMember.setNickname("同学" + i);
            //集合中元素为null
            list1.add(null);
        }
        model.put("list1", list1);


        //测试循环
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环内容 " + i);
        }

        model.put("list", list);


        //测试取值
        model.put("hello", "thymleaf");
        return "test";
    }
}
