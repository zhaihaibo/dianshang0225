package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.payment.config.AlipayConfig;

import com.atguigu.gmall.user.bean.OmsOrder;
import com.atguigu.gmall.user.bean.PaymentInfo;
import com.atguigu.gmall.user.service.OrderService;
import com.atguigu.gmall.user.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
   PaymentService paymentService;

    //支付宝回调，获取并更新
    @RequestMapping("alipay/callback/return")
    public String callback(HttpServletRequest request, String orderSn, BigDecimal totalAmount, ModelMap map) {

        // 验证签名  注意:支付宝2.0以后验签操作在同步回调中已经不起作用
        //         但是有这个过程，现在只发生在异步回调中！
        String sign = request.getParameter("sign");
        String out_trade_no = request.getParameter("out_trade_no");
        //从回调参数中获取，回调时间，回调内容，单号等，为了在数据库中更新支付信息！
        //先检查幂等性
        String payStatus = paymentService.checkDbPayStatus(out_trade_no);
        if(!payStatus.equals("success")) {
            String total_amount = request.getParameter("total_amount");
            String trade_no = request.getParameter("trade_no");
            String app_id = request.getParameter("app_id");

            String queryString = request.getQueryString();

            // 更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setTotalAmount(new BigDecimal(total_amount));
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(queryString);
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setOrderSn(out_trade_no);

            //是发送消息队列！进行应用解耦
            paymentService.sendPaymentSuccess(paymentInfo);
            //更新支付信息！
            paymentService.updatePaymentByOrderSn(paymentInfo);
        }

        return "redirect:/paySuccess.html";
    }

    //发送给支付宝的信息 以及 再数据库中存储支付信息
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipay(String orderSn , ModelMap modelMap, BigDecimal totalAmount){
        // 根据支付宝的客户端生成一个重定向地址，让客户重定向到支付宝支付页面
        OmsOrder omsOrder =  orderService.getOrderByOrderSn(orderSn);

        //封装公共参数
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
            alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
            alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 封装业务参数
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",orderSn);
        requestMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        requestMap.put("total_amount","0.01");
        requestMap.put("subject",omsOrder.getOmsOrderItems().get(0).getProductName());
        String requestMapJSON = JSON.toJSONString(requestMap);
        //填充业务参数
        alipayRequest.setBizContent(requestMapJSON);

        String form="";
            try {
                //根据请求参数生成一表单！
                form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
        System.out.println(form);
       //生成支付信息！
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(orderSn);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setSubject(omsOrder.getOmsOrderItems().get(0).getProductName());
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.addPayment(paymentInfo);

        //在给支付宝发送请求之前，发送一延迟队列，防止支付出错
        paymentService.sendPaymentStatusCheckQueue(paymentInfo,5);

        return form;

    }

    @RequestMapping("/mx/submit")
    public String mx(String orderSn , ModelMap modelMap, BigDecimal totalAmount){

        return null;

    }
    @RequestMapping("index")
    public String index(String orderSn , ModelMap modelMap, BigDecimal totalAmount){

        modelMap.put("orderSn",orderSn);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }

}
