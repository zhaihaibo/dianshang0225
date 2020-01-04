package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.annation.LoginRequired;
import com.atguigu.gmall.user.bean.OmsCartItem;
import com.atguigu.gmall.user.bean.PmsSkuInfo;
import com.atguigu.gmall.user.service.CartService;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.Cookie;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;

    //加这个注解是因为：进入拦截器中，想蹭一下拦截器中在域对象中放置的用户信息！
    @LoginRequired
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest Request, HttpServletResponse Response, ModelMap modelMap, OmsCartItem omsCartItem) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //查询用户是否登陆
        String memberId = (String) Request.getAttribute("memberId");
        String nickname = (String) Request.getAttribute("nickname");

        if (StringUtils.isBlank(memberId)) {
            //没有登陆，找cookie
            String catListCookie = CookieUtil.getCookieValue(Request, "cartListCookie", true);
            if (StringUtils.isNotBlank(catListCookie)) {
                omsCartItems = JSON.parseArray(catListCookie, OmsCartItem.class);
                for (OmsCartItem cartItem : omsCartItems) {
                    if (omsCartItem.getProductSkuId().equals(cartItem.getProductSkuId())) {
                        cartItem.setIsChecked(omsCartItem.getIsChecked());
                    }
                }
            }
        } else {
            //登陆了，找db
            omsCartItem.setMemberId(memberId);
            cartService.updataCartIsChecked(omsCartItem);
            //从缓存中获取
            omsCartItems = cartService.getCartCache(memberId);
        }
        modelMap.put("cartList", omsCartItems);
        modelMap.put("sumPrice", getPrice(omsCartItems));


        return "cartListInner";
    }

    @LoginRequired
    @RequestMapping("cartList")
    public String toTrade(HttpServletRequest Request, HttpServletResponse Response, ModelMap modelMap) {

        List<OmsCartItem> list = new ArrayList<>();
        //判断用户是否登陆
        String memberId = (String) Request.getAttribute("memberId");
        String nickname = (String) Request.getAttribute("nickname");
        //如果没有登陆，取cookie中的数据
        if (StringUtils.isBlank(memberId)) {
            String catListCookie = CookieUtil.getCookieValue(Request, "cartListCookie", true);
            if (StringUtils.isNotBlank(catListCookie)) {
                list = JSON.parseArray(catListCookie, OmsCartItem.class);
            }
        } else {
            //如果登陆，查缓存
            list = cartService.getCartCache(memberId);

        }

        modelMap.put("cartList", list);
        modelMap.put("sumPrice", getPrice(list));

        return "cartList";
    }

    private BigDecimal getPrice(List<OmsCartItem> omsCartItems) {

        BigDecimal sum = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            String isChecked = omsCartItem.getIsChecked();
            if (isChecked.equals("1")) {
                sum = sum.add(omsCartItem.getTotalPrice());
            }
        }
        return sum;


    }

    @LoginRequired
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest Request, HttpServletResponse Response, OmsCartItem omsCartItem) {
        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(omsCartItem.getProductSkuId(), Request.getRemoteAddr());

        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setIsChecked("1");
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductSkuId(pmsSkuInfo.getId());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setTotalPrice(pmsSkuInfo.getPrice().multiply(omsCartItem.getQuantity()));


        List<OmsCartItem> omsCartItems = new ArrayList<>();

        //加入购物车
        //判断用户是否登陆
        String memberId = (String) Request.getAttribute("memberId");
        String nickname = (String) Request.getAttribute("nickname");
        //没登陆在cookie中操作
        if (StringUtils.isBlank(memberId)) {
            //判断cookie是否有购物车数据
            String catListCookie = CookieUtil.getCookieValue(Request, "cartListCookie", true);
            if (StringUtils.isBlank(catListCookie)) {
                //如果cookie为空直接添加
                omsCartItems.add(omsCartItem);
            } else {
                //如果cookie中购物车不为空，看是更新还是新增
                omsCartItems = JSON.parseArray(catListCookie, OmsCartItem.class);
                //看是新车还是老车
                boolean b = if_new_cart(omsCartItems, omsCartItem);
                //如果b=true，购物车中没有，新增
                if (b) {
                    omsCartItems.add(omsCartItem);
                } else {
                    //如果b=false，购物车中有，更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            BigDecimal i= omsCartItem.getQuantity();
                            BigDecimal add = cartItem.getQuantity().add(omsCartItem.getQuantity());
                            cartItem.setQuantity(add);
                            cartItem.setQuantity(cartItem.getPrice().multiply(cartItem.getQuantity()));
                            break;
                        }
                    }
                }
            }
            String cookieJSON = JSON.toJSONString(omsCartItems);
            System.out.println(cookieJSON+"---------------");
            CookieUtil.setCookie(Request, Response, "cartListCookie", cookieJSON, 1000 * 60 * 60 * 24, true);//cookie工具类


        } else {
            //登陆后查询数据库

            omsCartItem.setMemberId(memberId);
            omsCartItem.setMemberNickname("windr");
//根据当前用户的memberid和skuid来判断用户是否添加过此商品
            OmsCartItem omsCartItemExist = cartService.isCartExist(omsCartItem);
            if (omsCartItemExist != null) {
                //更新
                omsCartItemExist.setQuantity(omsCartItemExist.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updataCartById(omsCartItemExist);

            } else {
                //新增
                cartService.insertCart(omsCartItem);
            }

        }

        return "redirect:/success.html";

    }

    private boolean if_new_cart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = true;
        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                b = false;
                break;
            }
        }
        return b;
    }





}
