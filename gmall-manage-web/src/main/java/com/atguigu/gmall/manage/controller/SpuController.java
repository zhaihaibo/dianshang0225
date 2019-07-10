package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manage.util.MyUploadUtil;
import com.atguigu.gmall.user.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.user.bean.PmsProductImage;
import com.atguigu.gmall.user.bean.PmsProductInfo;
import com.atguigu.gmall.user.bean.PmsProductSaleAttr;
import com.atguigu.gmall.user.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> saleAttrs = spuService.spuSaleAttrList(spuId);

        return saleAttrs;

    }

    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId) {
        List<PmsProductImage> images = spuService.spuImageList(spuId);
        return images;
    }


    @RequestMapping("spuList")

    public List<PmsProductInfo> spuList(String catalog3Id) {

        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);

        return pmsProductInfos;
    }

    @RequestMapping("baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = spuService.baseSaleAttrList();
        return pmsBaseSaleAttrs;

    }

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo productInfo) {
        spuService.saveSpuInfo(productInfo);
        return "success!";
    }

    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {


        // 调用图片服务器，上传图片，返回服务器的url图片地址
        String imgUrl = MyUploadUtil.uploadImage(multipartFile);
        return imgUrl;
    }
}
