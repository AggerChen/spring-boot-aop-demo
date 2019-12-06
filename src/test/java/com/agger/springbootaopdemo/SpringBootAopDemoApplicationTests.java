package com.agger.springbootaopdemo;

import com.agger.springbootaopdemo.utils.AESUtils;
import com.agger.springbootaopdemo.vo.DeptVO;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringBootAopDemoApplicationTests {

    @Test
    void contextLoads() {
        try {
            String json = JSON.toJSONString(new DeptVO(123,"研发部",""));
            System.out.println(AESUtils.encrypt(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
