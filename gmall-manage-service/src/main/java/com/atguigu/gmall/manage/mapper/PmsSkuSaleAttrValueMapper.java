package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.user.bean.PmsProductSaleAttr;
import com.atguigu.gmall.user.bean.PmsSkuInfo;
import com.atguigu.gmall.user.bean.PmsSkuSaleAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuSaleAttrValueMapper extends Mapper<PmsSkuSaleAttrValue> {


    List<PmsSkuSaleAttrValue> checkSkuByValueIds(@Param("join") String join);

    List<PmsSkuInfo> checkSkuByValueIdsTwo(@Param("join") String join);

    List<PmsSkuInfo> checkSkuBySpuId(@Param("spuId") String spuId);

    List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySkuId(@Param("spuId") String spuId, @Param("skuId") String skuId);
}
