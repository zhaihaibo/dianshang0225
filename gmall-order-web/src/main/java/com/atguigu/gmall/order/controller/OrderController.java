package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annation.LoginRequired;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.CartService;
import com.atguigu.gmall.user.service.OrderService;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;


    @LoginRequired(isNeededSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(String receiveAddressId, HttpServletRequest request, ModelMap map, String tradeCode) {
        //查询用户id，根据id，查购物车列表
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //生成订单时避免重复提交
        boolean b = orderService.checkOrderCode(memberId, tradeCode);
        if (b == true) {
            //根据购物车列表和收获地址生成订单
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getMemberAddressesByAddressId(receiveAddressId);
            List<OmsCartItem> cartItems = cartService.getCartCache(memberId);

            //将订单保存到数据库
            OmsOrder omsOrder = new OmsOrder();
            //根据收获地址信息填写订单
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String formatDate = simpleDateFormat.format(new Date());
            long l = System.currentTimeMillis();
            String orderSn = "gmall" + formatDate + l;
            omsOrder.setOrderSn(orderSn);

            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setOrderType(0);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setStatus("0");
            omsOrder.setSourceType(0);
            omsOrder.setTotalAmount(getCartSumPrice(cartItems));

            //根据购物车信息填写订单！

            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem cartItem : cartItems) {
                if (cartItem.getIsChecked().equals("1")) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //验证缓存中的价格和数据库中的价格是否一样(验价)
                    BigDecimal priceFromCache = cartItem.getPrice();
                    String productSkuId = cartItem.getProductSkuId();
                    PmsSkuInfo skuInfoById = skuService.getSkuInfoById(productSkuId, "");
                    BigDecimal priceFromDB = skuInfoById.getPrice();
                    //比较两者价格
                    int i = priceFromCache.compareTo(priceFromDB);
                    //相等就赋值
                    if (i == 0) {
                        omsOrderItem.setProductPrice(cartItem.getPrice());
                    } else {
                        map.put("err", "价格已经发生改变");
                        return "tradeFail";
                    }
                    //验库存
                    omsOrderItem.setProductQuantity(cartItem.getQuantity());
                    omsOrderItem.setPromotionAmount(omsOrderItem.getProductQuantity().multiply(omsOrderItem.getProductPrice()));
                    omsOrderItem.setProductSkuId(cartItem.getProductSkuId());
                    omsOrderItem.setProductPic(cartItem.getProductPic());
                    omsOrderItem.setProductName(cartItem.getProductName());
                    omsOrderItem.setProductId(cartItem.getProductId());
                    omsOrderItem.setProductCategoryId(cartItem.getProductCategoryId());
                    omsOrderItem.setOrderSn(orderSn);
                    omsOrderItems.add(omsOrderItem);
                }
            }
            //封装omsOrder中的orderItem信息！
            omsOrder.setOmsOrderItems(omsOrderItems);
            //把订单存入数据库
            orderService.addOrder(omsOrder);
            //删除购物车数据
            // 重定向到支付页面
            return "redirect:http://payment.gmall.com:8090/index.html?orderSn="+orderSn+"&totalAmount="+getCartSumPrice(cartItems);// 重定向到支付页面

        } else {
            map.put("err", "不能提交多个订单！");
            return "tradeFail";
        }

    }

    @LoginRequired(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map) {
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickname");

        //根据用户id查询用户在购物车中选中的商品,从缓存中
        List<OmsCartItem> omsCartItems = cartService.getCartCache(memberId);

        //把商品对象转化成临时的订单对象
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                omsOrderItem.setRealAmount(omsOrderItem.getProductPrice().multiply(omsOrderItem.getProductQuantity()));

                omsOrderItems.add(omsOrderItem);
            }
        }

        //查询用户收货列表
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getMemberAddressesById(memberId);
        map.put("totalAmount", getTotalAmount(omsOrderItems));
        map.put("orderDetailList", omsOrderItems);
        map.put("userAddressList", umsMemberReceiveAddresses);

        //为了防止重复提交订单，通过生成交易码进行验证的方式
            String tradeCode = orderService.genTradeCode(memberId);
        map.put("tradeCode", tradeCode);
        return "trade";
    }

    private Object getTotalAmount(List<OmsOrderItem> omsOrderItems) {

        BigDecimal bigDecimal = new BigDecimal("0");
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            BigDecimal realAmount = omsOrderItem.getRealAmount();
            bigDecimal = bigDecimal.add(realAmount);
        }
        return bigDecimal;
    }

    private BigDecimal getCartSumPrice(List<OmsCartItem> omsCartItems) {

        BigDecimal sum = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            String isChecked = omsCartItem.getIsChecked();
            if (isChecked.equals("1")) {
                sum = sum.add(omsCartItem.getTotalPrice());
            }
        }
        return sum;
    }


}


