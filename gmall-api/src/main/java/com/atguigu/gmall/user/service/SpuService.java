package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.user.bean.PmsProductImage;
import com.atguigu.gmall.user.bean.PmsProductInfo;
import com.atguigu.gmall.user.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
     List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(PmsProductInfo productInfo);


    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);
}
