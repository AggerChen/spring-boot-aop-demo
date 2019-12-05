package com.agger.springbootaopdemo.aop;

import com.agger.springbootaopdemo.utils.AESUtils;
import com.agger.springbootaopdemo.vo.BaseVO;
import com.agger.springbootaopdemo.vo.ResultVO;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @program: SecretAOPController
 * @description: 切面加密解密
 * @author: chenhx
 * @create: 2019-12-05 13:43
 **/
@Aspect
@Component
public class SecretAOPController {

    // 是否进行加密解密，通过配置文件注入（不配置默认为true）
    @Value("${isSecret:true}")
    boolean isSecret;

    // 定义切点
    @Pointcut("@within(com.agger.springbootaopdemo.annotation.Secret)")
    public void pointcut(){}

    // 环绕切面
    @Around("pointcut()")
    public ResultVO around(ProceedingJoinPoint point){
        // 获取方法参数
       Object[] args = point.getArgs();
       ResultVO result = null;
        try {
            for (int i = 0; i < args.length; i++) {
                // 如果是BaseVo类型则说明使用了加密字符串encryptStr传递的加密参数
                if(args[i] instanceof BaseVO){
                    BaseVO base = (BaseVO) args[i];
                    String encryptStr = base.getEncryptStr();
                    // 加密字符串是否为空
                    if(StringUtils.isNotBlank(encryptStr)){
                        // 解密
                        String json = AESUtils.decrypt(encryptStr);
                        // 转换vo
                        BaseVO vo = JSON.parseObject(json, (Type) args[i].getClass());
                        args[i] = vo;
                    }
                }
                // 其他类型，比如基本数据类型、包装类型就不使用加密解密了
            }

            // 执行请求
            result = (ResultVO) point.proceed(args);

            // 判断配置是否需要返回加密
            if(isSecret){
                // 获取返回值json字符串
                String jsonString = JSON.toJSONString(result.getData());
                // 加密
                String s = AESUtils.encrypt(jsonString);
                result.setData(s);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }
}
