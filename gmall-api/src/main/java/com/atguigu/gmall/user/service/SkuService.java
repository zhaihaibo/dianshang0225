package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PmsProductSaleAttr;
import com.atguigu.gmall.user.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySkuId(String spuId, String skuId);

    List<PmsSkuInfo> checkSkuBySpuId(String spuId);


    //获取skuid的方法二
    String checkSkuByValueIdsTwo(String[] ids);

    String checkSkuByValueIds(String[] ids);

    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfoByIdFromDB(String skuId);

    PmsSkuInfo getSkuInfoById(String skuid, String ip);


    List<PmsSkuInfo> getAllSku();


}
