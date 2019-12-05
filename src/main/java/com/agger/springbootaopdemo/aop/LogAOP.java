//package com.agger.springbootaopdemo.aop;
//
//import com.agger.springbootaopdemo.vo.ResultVO;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * @program: LogAOP
// * @description: 日志切面
// * @author: chenhx
// * @create: 2019-12-03 15:20
// **/
//@Aspect         //切面注解
//@Component
//@Slf4j
//public class LogAOP {
//
//    /**
//     * @Title: logPoint
//     * @Description: 定义切入点
//     * @author chenhx
//     * @date 2019-12-03 15:43:42
//     */
//    // 匹配指定注解注释的类
//     @Pointcut("@within(com.agger.springbootaopdemo.annotation.MyLog)")
//
//    // 匹配指定注解注释的方法
////    @Pointcut("@annotation(com.agger.springbootaopdemo.annotation.MyLog)")
//    public void logPoint(){};
//
//
//    /**
//     * @Title: doBefor
//     * @Description: 切点前执行
//     * @author chenhx
//     * @date 2019-12-03 15:48:35
//     */
//    @Before("logPoint()")
//    public void doBefore(JoinPoint point) throws Throwable{
//        log.info("切面before");
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//    }
//
//    /**
//     * @Title: doAfter
//     * @Description: 切点后执行
//     * @author chenhx
//     * @date 2019-12-03 15:48:46
//     */
//    @After("logPoint()")
//    public void doAfter(JoinPoint joinPoint) throws Throwable{
//        log.info("切面after" );
//    }
//
//    /**
//     * @Title: doAfterReturn
//     * @Description: 切点后执行
//     * @author chenhx
//     * @date 2019-12-03 15:48:46
//     */
//    @AfterReturning(pointcut = "logPoint()",returning = "ret")
//    public void doAfterReturn(Object ret) throws Throwable{
//       log.info("切面afterReturning,返回内容" + ret);
//    }
//
//    /**
//     * @Title: doAfterThrowing
//     * @Description:
//     * @author chenhx
//     * @date 2019-12-05 11:23:32
//     */
//    @AfterThrowing(pointcut = "logPoint()",throwing = "e")
//    public void doAfterThrowing(Throwable e){
//        log.error("异常信息" + e.getMessage());
//    }
//
//    /**
//     * @Title: around
//     * @Description: 环绕通知
//     * @author chenhx
//     * @date 2019-12-05 11:34:35
//     */
//    @Around("logPoint()")
//    public ResultVO around(ProceedingJoinPoint pjp){
//        log.info("环绕通知");
//        ResultVO result = null;
//        try {
//             result = (ResultVO) pjp.proceed();
//            log.info("环绕返回通知" + result);
//            result.setData(123);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        return result;
//
//    }
//}
