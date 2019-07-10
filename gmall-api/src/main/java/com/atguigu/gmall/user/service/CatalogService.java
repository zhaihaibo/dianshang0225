package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PmsBaseCatalog1;
import com.atguigu.gmall.user.bean.PmsBaseCatalog2;
import com.atguigu.gmall.user.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {

    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    List<PmsBaseCatalog1> getCatalog1();


    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
