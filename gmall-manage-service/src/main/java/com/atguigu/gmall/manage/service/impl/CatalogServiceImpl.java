package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.atguigu.gmall.user.bean.PmsBaseCatalog1;
import com.atguigu.gmall.user.bean.PmsBaseCatalog2;
import com.atguigu.gmall.user.bean.PmsBaseCatalog3;
import com.atguigu.gmall.user.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;

    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;

    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;


    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);

        return pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);

    }

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {

        List<PmsBaseCatalog1> pmsBaseCatalog1 = pmsBaseCatalog1Mapper.selectAll();
        for (PmsBaseCatalog1 baseCatalog1 : pmsBaseCatalog1) {
            String id1 = baseCatalog1.getId();
            PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
            pmsBaseCatalog2.setCatalog1Id(id1);
            List<PmsBaseCatalog2> two = pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
            baseCatalog1.setCatalog2s(two);
            for (PmsBaseCatalog2 baseCatalog2 : two) {
                String id2 = baseCatalog2.getId();
                PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
                pmsBaseCatalog3.setCatalog2Id(id2);
                List<PmsBaseCatalog3> three = pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
                baseCatalog2.setCatalog3List(three);
            }
        }
        return pmsBaseCatalog1;
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        return pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
    }


}
