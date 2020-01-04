package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.annation.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class IndexController {

    @LoginRequired
    @RequestMapping("index")
    public String toIndex() {
        return "index";
    }


}
