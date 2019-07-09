package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.user.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.user.bean.PmsBaseAttrValue;
import com.atguigu.gmall.user.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
@Service
public class AtterServiceImpl implements AttrService {
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {


        
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        //加入平台属性值
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            String id = baseAttrInfo.getId();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(id);
            List<PmsBaseAttrValue> baseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(baseAttrValues);
        }

        return pmsBaseAttrInfos;


    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id1 = pmsBaseAttrInfo.getId();
        if (StringUtils.isBlank(id1)) {
            pmsBaseAttrInfoMapper.insert(pmsBaseAttrInfo);
            //遍历info中的PmsBaseAttrValue集合，在插入之前，把外键设置进去！
            String id = pmsBaseAttrInfo.getId();

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();

            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(id);
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else {
            //先删除
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(id1);
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
            //再添加
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(id1);
                pmsBaseAttrValueMapper.insertSelective(baseAttrValue);
            }

        }




    }


    @Override
    public List<PmsBaseAttrValue> getAttrValue(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);

        List<PmsBaseAttrValue> select = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

        return select;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueByValueIds(HashSet<String> set) {
/*
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setId(join);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        List<PmsBaseAttrInfo> baseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        return baseAttrInfos;*/

        String join = StringUtils.join(set, ",");
       List<PmsBaseAttrInfo> pmsBaseAttrInfos =  pmsBaseAttrInfoMapper.selectAttrValueByValueIds(join);
        return  pmsBaseAttrInfos;

    }


}
