package com.agger.springbootaopdemo.aop;

import com.agger.springbootaopdemo.annotation.Secret;
import com.agger.springbootaopdemo.utils.AESUtils;
import com.agger.springbootaopdemo.vo.ResultVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @program: SecretAOPController
 * @description: 切面加密解密
 * @author: chenhx
 * @create: 2019-12-05 13:43
 **/
@Aspect
@Component
@Slf4j
public class SecretAOPController {

    // 是否进行加密解密，通过配置文件注入（不配置默认为true）
    @Value("${isSecret:true}")
    boolean isSecret;

    // 定义切点,使用了@Secret注解的类 或 使用了@Secret注解的方法
    @Pointcut("@within(com.agger.springbootaopdemo.annotation.Secret) || @annotation(com.agger.springbootaopdemo.annotation.Secret)")
    public void pointcut(){}

    // 环绕切面
    @Around("pointcut()")
    public ResultVO around(ProceedingJoinPoint point){
        ResultVO result = null;

        // 获取被代理方法参数
        Object[] args = point.getArgs();

        // 获取被代理对象
        Object target = point.getTarget();

        // 获取通知签名
        MethodSignature signature = (MethodSignature )point.getSignature();

        try {
            // 获取被代理方法
            Method pointMethod = target.getClass().getMethod(signature.getName(), signature.getParameterTypes());
            // 获取被代理方法上面的注解@Secret
            Secret secret = pointMethod.getAnnotation(Secret.class);
            // 被代理方法上没有，则说明@Secret注解在被代理类上
            if(secret==null){
                secret = target.getClass().getAnnotation(Secret.class);
            }

            if(secret!=null){
                // 获取注解上声明的加解密类
                Class clazz = secret.value();
                // 获取注解上声明的加密参数名
                String encryptStrName = secret.encryptStrName();

                for (int i = 0; i < args.length; i++) {
                    // 如果是clazz类型则说明使用了加密字符串encryptStr传递的加密参数
                    if(clazz.isInstance(args[i])){
                        Object cast = clazz.cast(args[i]);      //将args[i]转换为clazz表示的类对象
                        // 通过反射，执行getEncryptStr()方法，获取加密数据
                        Method method = clazz.getMethod(getMethedName(encryptStrName));
                        // 执行方法，获取加密数据
                        String encryptStr = (String) method.invoke(cast);
                        // 加密字符串是否为空
                        if(StringUtils.isNotBlank(encryptStr)){
                            // 解密
                            String json = AESUtils.decrypt(encryptStr);
                            // 转换vo
                            args[i] = JSON.parseObject(json, (Type) args[i].getClass());
                        }
                    }
                    // 其他类型，比如基本数据类型、包装类型就不使用加密解密了
                }
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

        } catch (NoSuchMethodException e) {
            log.error("@Secret注解指定的类没有字段:encryptStr,或encryptStrName参数字段不存在");
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }

    // 转化方法名
    private String getMethedName(String name){
        String first = name.substring(0,1);
        String last = name.substring(1);
        first = StringUtils.upperCase(first);
        return "get" + first + last;
    }
}
