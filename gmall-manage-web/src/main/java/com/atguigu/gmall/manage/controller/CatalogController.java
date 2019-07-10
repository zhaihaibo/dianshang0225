package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.user.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.user.bean.PmsBaseCatalog1;
import com.atguigu.gmall.user.bean.PmsBaseCatalog2;
import com.atguigu.gmall.user.bean.PmsBaseCatalog3;
import com.atguigu.gmall.user.service.CatalogService;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

@CrossOrigin
@RestController
public class CatalogController {

    @Reference
    CatalogService catalogService;


    @RequestMapping("index")
    public String test() {
        return "卧槽！";
    }


    @RequestMapping("getCatalog1")
    public List<PmsBaseCatalog1> getCatalog1() throws Exception {
        List<PmsBaseCatalog1> catalog1 = catalogService.getCatalog1();
        String json = JSON.toJSONString(catalog1);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/a.json"));
        fileOutputStream.write(json.getBytes());
        return catalog1;
    }


    @RequestMapping("getCatalog2")
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        List<PmsBaseCatalog2> catalog2 = catalogService.getCatalog2(catalog1Id);
        return catalog2;
    }


    @RequestMapping("getCatalog3")
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        return catalogService.getCatalog3(catalog2Id);
    }


}
