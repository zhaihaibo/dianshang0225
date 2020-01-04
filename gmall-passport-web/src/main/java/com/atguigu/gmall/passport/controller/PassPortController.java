package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.passport.util.JwtUtil;
import com.atguigu.gmall.user.bean.UmsMember;
import com.atguigu.gmall.user.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PassPortController {
    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code,ModelMap map,HttpServletRequest request){


        //通过授权码，再加入服务器密钥，交换access_token
        String url3 = "https://api.weibo.com/oauth2/access_token";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","3529198529");
        paramMap.put("client_secret","1fb81d15d48b7631f19d1b283d770219");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code",code);
        //access_token,必须用post请求，使用工具类进行拼接！json封装的有用户的uid，token信息！
        String json = HttpclientUtil.doPost(url3, paramMap);
        String token = "";
        if (StringUtils.isNotBlank(json)) {
            //把json解析成hashmap，以便于后文获取信息
            HashMap<String, String> hashMap = JSON.parseObject(json, new HashMap<String, String>().getClass());

            String access_token = hashMap.get("access_token");
            String uid = hashMap.get("uid");
            //第四个url，show用户信息
            String JSONUser = HttpclientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid);
            HashMap<String, Object> userMap = JSON.parseObject(JSONUser, new HashMap<String, Object>().getClass());

            //将第三方账号存入数据库中
            UmsMember umsMember = new UmsMember();
            umsMember.setUsername((String) userMap.get("name"));
            umsMember.setNickname((String) userMap.get("screen_name"));
            umsMember.setAccessCode(code);
            umsMember.setAccessToken(access_token);
            umsMember.setSourceUid(Long.parseLong((String) userMap.get("idstr")));
            umsMember.setCreateTime(new Date());
            umsMember.setCity((String) userMap.get("city"));
            String gender = (String) userMap.get("gender");

            int genderi = 0;
            if (gender.equals("m")) {
                genderi = 1;
            }
            if (gender.equals("f")) {
                genderi = 2;
            }
            umsMember.setGender(genderi);
            umsMember.setSourceType(2);
            UmsMember umsMemberReturn = userService.addOauthUser(umsMember);

            //生成token

            String memberId = umsMemberReturn.getId();
            String nickname = umsMemberReturn.getNickname();

            String salt = request.getHeader("x-forward-for");
            if (StringUtils.isBlank(salt)) {
                salt = request.getRemoteAddr();
                if (StringUtils.isBlank(salt)) {
                    return "redirect:http://search.gmall.com:8083/index?newToken=" + token;

                }
            }

            HashMap<String, Object> jwtUserMap = new HashMap<>();
            jwtUserMap.put("memberId", memberId);
            jwtUserMap.put("nickname", nickname);
            token = JwtUtil.encode("gmall", jwtUserMap, salt);
        }
        return "redirect:http://search.gmall.com:8083/index?newToken="+token;

    }


    @RequestMapping("index")
    public String index(String token, ModelMap modelMap, String ReturnUrl) {


        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {

        //根据登陆表单提交的内容，在数据库中查找
        String token = "failed";
        UmsMember umsMemberFromDB = userService.login(umsMember);
        String salt = "";
        if (umsMemberFromDB != null) {
            salt = request.getHeader("x-forward-for");
            if (StringUtils.isBlank(salt)) {
                salt = request.getRemoteAddr();
                if (StringUtils.isBlank(salt)) {
                    return token;
                }
            }
            HashMap map = new HashMap();

            map.put("memberId", umsMemberFromDB.getId());
            map.put("nickname", umsMemberFromDB.getNickname());

            //把查找结果通过jwt编码成token；
            token = JwtUtil.encode("gmall", map, salt);

            //把token在缓存中备份一个
            String id = umsMemberFromDB.getId();
            userService.putTokenToCache(token,id);
            return token;
        }



        return token;
    }


    @RequestMapping("verify")
    @ResponseBody
    public Map<String, String> verify(String token , HttpServletRequest request) {

        HashMap<String, String> hashMap = new HashMap<>();

        String salt = request.getHeader("x-forward-for");
        if (StringUtils.isBlank(salt)){
            salt = request.getRemoteAddr();
            if (StringUtils.isBlank(salt)){
                hashMap.put("success","failed");
                return  hashMap;
            }
        }

        Map<String, Object> gmallMap= JwtUtil.decode(token, "gmall", salt);
        if (gmallMap!=null){
            hashMap.put("success","success");
            hashMap.put("memberId", (String) gmallMap.get("memberId"));
            hashMap.put("nickname", (String) gmallMap.get("nickname"));
        }else {
            hashMap.put("success","failed");
            return  hashMap;
        }



        return hashMap;
    }

}
