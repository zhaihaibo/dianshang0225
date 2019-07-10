package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.user.bean.PmsBaseAttrValue;

import java.util.HashSet;
import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);


    List<PmsBaseAttrValue> getAttrValue(String attrId);

    List<PmsBaseAttrInfo> getAttrValueByValueIds(HashSet<String> set);
}
