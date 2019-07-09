package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.AttrService;
import com.atguigu.gmall.user.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller

public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String toList(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        //通过keyword获取es数据库中所有与keyword相匹配的PmsSearchSkuInfo
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.search(pmsSearchParam);

        //获取所有搜索后的商品所具有的平台属性值的id并通过set集合去重
        //因为每一个平台属性值， 都必须有至少一个商品与之对应！
        HashSet<String> set = new HashSet<>();
        if (pmsSearchSkuInfos != null && pmsSearchSkuInfos.size() > 0) {
            for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
                List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
                for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                    String valueId = pmsSkuAttrValue.getValueId();
                    set.add(valueId);
                }
            }
        }
        //得到所有平台属性和属性值，并在页面显示！
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = null;
        if (set != null && set.size() > 0) {
            pmsBaseAttrInfos = attrService.getAttrValueByValueIds(set);
        }
        //显示搜索后的商品信息
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

/*        //面包屑集合
        List<PmsCrumb> pmsCrumbs = new ArrayList<>();
        String[] valueId1 = pmsSearchParam.getValueId();
        if (valueId1!=null&&valueId1.length>0) {
            for (String s : valueId1) {
                PmsCrumb pmsCrumb = new PmsCrumb();
                //设置面包屑的地址属性
                pmsCrumb.setUrlParam(getUrlParam(pmsSearchParam,s));
                //设置面包屑的name属性：根据pmsBaseAttrInfos查到所有的pmsBaseAttrInfo
                for (PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfos) {
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    //获取pmsBaseAttrInfo中的pmsBaseAttrValue
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        //比较两者id是否相等
                        if (s.equals(pmsBaseAttrValue.getId())) {
                            String valueName = pmsBaseAttrValue.getValueName();
                            pmsCrumb.setValueName(valueName);
                        }
                    }
                }
            //把每一个面包屑放进集合
                pmsCrumbs.add(pmsCrumb);
            }
        }*/

        //在界面上去除被选中的平台属性
        String[] valueId = pmsSearchParam.getValueId();
        //面包屑的集合，因为去除属性和面包屑代码相似，所以两者进行融合
        List<PmsCrumb> pmsCrumbs = new ArrayList<>();

        if (valueId != null && valueId.length > 0) {
            for (String s : valueId) {

                PmsCrumb pmsCrumb = new PmsCrumb();
                //设置面包屑的地址属性
                pmsCrumb.setUrlParam(getUrlParam(pmsSearchParam,s));
                //查询PmsBaseAttrInfo集合
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String id = pmsBaseAttrValue.getId();
                        //比较选中的id和sku下所有的属性值id是不是相等！
                        if (id.equals(s)) {
                            String valueName = pmsBaseAttrValue.getValueName();
                            pmsCrumb.setValueName(valueName);
                            iterator.remove();//这里删除的是整个pmsBaseAttrInfo！
                        }
                    }
                }
                //把每一个指定valueId的面包屑对象放入集合！
                pmsCrumbs.add(pmsCrumb);
            }
        }
        //显示去除搜索后选中后剩余的平台属性，和值
        modelMap.put("attrList", pmsBaseAttrInfos);

        modelMap.put("attrValueSelectedList", pmsCrumbs);

        //点击属性值时跳转
        String currentStr = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", currentStr);
        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String... valueIdForCrumb) {
        String currentUrl = "";
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(currentUrl)) {
                currentUrl = currentUrl + "&";
            }
            currentUrl = currentUrl + "keyword=" + keyword;
        }
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(currentUrl)) {
                currentUrl = currentUrl + "&";
            }
            currentUrl = currentUrl + "catalog3Id=" + catalog3Id;
        }
        String[] valueIds = pmsSearchParam.getValueId();
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                //可变长度字符串会被自动封装成一个string类型数组！默认为空数组！
                if ((valueIdForCrumb == null || valueIdForCrumb.length == 0) || (valueIdForCrumb != null && valueIdForCrumb.length > 0 && !valueId.equals(valueIdForCrumb[0]))) {
                    currentUrl = currentUrl + "&valueId=" + valueId;
                }
            }
        }

        return currentUrl;
    }

}
