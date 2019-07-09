package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PmsSearchParam;
import com.atguigu.gmall.user.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam);
}
