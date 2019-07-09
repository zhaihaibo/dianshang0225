package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;

    public List<PmsProductInfo> spuList( String catalog3Id){
        PmsProductInfo pmsProductInfo =  new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
      return   pmsProductInfoMapper.select(pmsProductInfo);
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {

        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsBaseSaleAttrMapper.selectAll();
        return pmsBaseSaleAttrs;
    }

    @Override
    public void saveSpuInfo(PmsProductInfo productInfo) {
        String productName = productInfo.getProductName();

        pmsProductInfoMapper.insert(productInfo);

        String id = productInfo.getId();
        List<PmsProductImage> spuImageList = productInfo.getSpuImageList();
        List<PmsProductSaleAttr> spuSaleAttrList = productInfo.getSpuSaleAttrList();

        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {

            pmsProductSaleAttr.setProductId(id);
            pmsProductSaleAttrMapper.insert(pmsProductSaleAttr);

            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(id);
                pmsProductSaleAttrValueMapper.insert(pmsProductSaleAttrValue);
            }
        }
        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(id);
            pmsProductImageMapper.insert(pmsProductImage);
        }



    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();

        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> select = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        for (PmsProductSaleAttr saleAttr : select) {
            String id = saleAttr.getSaleAttrId();
            PmsProductSaleAttrValue attrValue = new PmsProductSaleAttrValue();
            attrValue.setProductId(spuId);
            attrValue.setSaleAttrId(id);
            List<PmsProductSaleAttrValue> select1 = pmsProductSaleAttrValueMapper.select(attrValue);
            saleAttr.setSpuSaleAttrValueList(select1);
        }
        return select;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {


        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);

        return pmsProductImages;
    }


}
