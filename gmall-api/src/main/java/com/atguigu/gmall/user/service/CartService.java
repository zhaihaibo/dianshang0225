package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem isCartExist(OmsCartItem omsCartItem);


    void updataCartById(OmsCartItem omsCartItemExist);

    void insertCart(OmsCartItem omsCartItem);

    List<OmsCartItem> getCartCache(String memberId);

    void updataCartIsChecked(OmsCartItem omsCartItem);
}
