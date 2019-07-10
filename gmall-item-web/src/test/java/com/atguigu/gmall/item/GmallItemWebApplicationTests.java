package com.atguigu.gmall.item;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.service.SkuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallItemWebApplicationTests {


    @Reference
    SkuService skuService;

    @Test
    public void contextLoads() {

        String[] ids = new String[4];
        ids[0] = "63";
        ids[1] = "66";
        ids[2] = "71";
        ids[3] = "72";
        String s = skuService.checkSkuByValueIds(ids);

        System.out.println(s);
    }

    @Test
    public void test1() {
        StringBuffer stringBuffer = new StringBuffer();
        for (char i = 'A'; i <= 'Z'; i++) {
            System.out.println(stringBuffer.append(stringBuffer.append(i)));


        }
    }


}
