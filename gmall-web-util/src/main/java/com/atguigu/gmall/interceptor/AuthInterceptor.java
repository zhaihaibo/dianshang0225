package com.atguigu.gmall.interceptor;

import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.annation.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

            HandlerMethod hm = (HandlerMethod)handler;
            LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

            if(methodAnnotation==null){
                return  true;
            }

            String token = "";

            // cookie中携带的token
            String oldToken = CookieUtil.getCookieValue(request,"oldToken",true);
            if(StringUtils.isNotBlank(oldToken)){
                token = oldToken;
            }
            // 地址栏中携带的token
            String newToken = request.getParameter("newToken");
            if(StringUtils.isNotBlank(newToken)){
                token = newToken;
            }
            // 如果没有认证通过的用户需要拦截并且发送到登录页面
            String ReturnUrl = request.getRequestURL().toString();

            if(StringUtils.isNotBlank(token)){
                String success = "";
                success = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token="+token);// 获取请求中的token，请求
                HashMap hashMap = JSON.parseObject(success, new HashMap<String, String>().getClass());
                if(!hashMap.get("success").equals("success")){
                    response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+ReturnUrl);
                }

                request.setAttribute("memberId",hashMap.get("memberId"));
                request.setAttribute("nickname",hashMap.get("nickname"));
                // 如果认证身份通过需要将token写入cookie
                CookieUtil.setCookie(request,response,"oldToken",token,60*30,true);
            }else{
                if(methodAnnotation.isNeededSuccess()) {
                    response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + ReturnUrl);
                    return false;
                }
            }

            return true;
        }
}
