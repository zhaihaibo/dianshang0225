package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.user.bean.PmsBaseAttrValue;
import com.atguigu.gmall.user.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class AttrController {


    @Reference
    AttrService attrService;

    @RequestMapping("attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        List<PmsBaseAttrInfo> list = attrService.attrInfoList(catalog3Id);

        return list;

    }

    @RequestMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {

        attrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    @RequestMapping("getAttrValueList")
    public List<PmsBaseAttrValue> updata(String attrId) {
        List<PmsBaseAttrValue> attrInfo = attrService.getAttrValue(attrId);

        return attrInfo;
    }


}
