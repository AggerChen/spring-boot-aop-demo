## SpringBoot自定义注解使用AOP实现请求参数解密以及响应数据加密
### 一、前言
本篇文章将依托与SpringBoot平台，自定义注解用来标识接口请求是否实现加密解密。使用AOP切面来具体操作解密加密，实现对源代码的低耦合，不在原基础上做很大的改动。

> 本篇文章的所有示例，都上传到我的github中，欢迎大家拉取测试，欢迎star [github](https://github.com/AggerChen/spring-boot-aop-demo)

**实现要求：**
1. 自定义一个注解@Secret，用来标识需要实现加密解密
    - 作用在Controller类上，表示此Controller类的所有接口都实现加密解密
    - 作用来单一方法上，表示此接口方法需要实现加密解密
 2. 使用AOP切面编程实现
    - 在接口方法执行之前将前端的加密参数解密并重新赋给接口参数
    - 在接口方法响应之后，将返回的数据进行加密返回
3. 在配置文件中配置，是否开启全局的加密解密操作

**实现流程：**
![](https://github.com/AggerChen/imageLibrary/blob/master/spring-boot-aop-demo/Image.png)
1. 前端请求的接口将请求参数json通过AES加密生成加密字符串，然后将加密字符串通过名为encyptStr字段传递给后端。
2. AOP前置方法拦截，将encyptStr字符串通过AES解密得到原始请求参数json，将json映射为请求方法的参数对象User。
3. 接口通过参数成功响应，并将响应数据直接返回。
4. AOP后置方式拦截，将响应参数data字段里的数据AES加密，并返回给前端
5. 前端收到请求响应，通过code判断请求是否成功，AES加密data字段得到需要的数据。

### 二、实现操作
#### 1. 创建SpringBoot项目
创建一个SpringBoot项目，导入必要的maven依赖。
- 使用AOP切面需要导入AOP的启动器
- lombok是一个通过注解简化代码的工具，在idea中使用需要安装lombok插件
- json转换工具，apache工具类
    
> pom.xml
```xml
<!-- web依赖 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- AOP切面依赖 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- lombok工具 -->
<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<optional>true</optional>
</dependency>

<!-- json操作类 -->
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>fastjson</artifactId>
	<version>1.2.52.sec06</version>
</dependency>
<!-- String工具包 -->
<dependency>
	<groupId>org.apache.commons</groupId>
	<artifactId>commons-lang3</artifactId>
	<version>3.9</version>
</dependency>
```

#### 2. 自定注解@Secret
我们通过自定义的注解，来标识类或接口，告诉AOP哪些类或方法需要执行加密解密操作，更加的灵活。
> Secret.java
```java
package com.agger.springbootaopdemo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @classname: Secret
 * @description: 自定义注解，用来标识请求类 或者方法是否使用AOP加密解密
 * @author chenhx
 * @date 2019-12-05 13:48:03
 */
@Target({ElementType.TYPE,ElementType.METHOD})              // 可以作用在类上和方法上
@Retention(RetentionPolicy.RUNTIME)                               // 运行时起作用
public @interface Secret {

    // 参数类（用来传递加密数据,只有方法参数中有此类或此类的子类才会执行加解密）
    Class value();

    // 参数类中传递加密数据的属性名，默认encryptStr
    String encryptStrName() default "encryptStr";
}
```
自定义注解很简单，只需要确定注解的作用位置和运行时机。其中有两个变量value和encryptStrName。
value没有默认值，是必传的参数，用来表示需要加解密的参数类或父类。AOP中或用到。
encryptStrName默认值为"encryptStr"，用来表示前端传递的加密参数名称是什么，value类中必须存在此字段

#### 3. Controller中使用
定义好@Secret注解后，我们就可以在Controller中使用了，不过现在只相当于是一个标注，还没有起任何作用，需要我们再定义好AOP后才会起作用。
1. @Secret注解作用来类上
> UserController.java
```java
package com.agger.springbootaopdemo.controller;

import com.agger.springbootaopdemo.annotation.Secret;
import com.agger.springbootaopdemo.vo.BaseVO;
import com.agger.springbootaopdemo.vo.ResultVO;
import com.agger.springbootaopdemo.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @program: UserController
 * @description: 用户控制类
 * @author: chenhx
 * @create: 2019-12-03 15:22
 **/
@Secret(BaseVO.class)                             //接口参数和返回要进行加解密
@RestController
@RequestMapping("user")
public class UserController {
    
    //采用内部类的实例代码块方式初始化map
    HashMap<Integer, UserVO> userMap = new HashMap<Integer, UserVO>(){
        {
            put(1,new UserVO(1,"张三"));
            put(2,new UserVO(2,"李四"));
            put(3,new UserVO(3,"王五"));
        }
    };

    // 通过id查询用户
    @GetMapping("getUserName/{id}")
    public ResultVO getUserName(@PathVariable("id")  Integer id){
        return new ResultVO(0,"查询成功",userMap.get(id));
    }

    // 通过name查询用户id
    @GetMapping("getUserId")
    public ResultVO getUserId(@RequestParam  String name){
        Iterator<Map.Entry<Integer, UserVO>> iterator = userMap.entrySet().iterator();
        UserVO u = null;
        while (iterator.hasNext()){
            Map.Entry<Integer, UserVO> entry = iterator.next();
            if(entry.getValue().getName().equals(name)){
                u = entry.getValue();
                break;
            }
        }
        return new ResultVO(0,"查询成功",u);
    }

    // 新增用户
    @PostMapping("addUser")
    public ResultVO addUser(@RequestBody UserVO user){
        return new ResultVO(0,"新增成功",user);
    }

    // 更改用户
    @PostMapping("updateUser")
    public ResultVO updateUser(@RequestBody UserVO user) throws Throwable {
        if(user==null||user.getId()==null){
            throw new NullPointerException();
        }else{
            return new ResultVO(0,"修改成功",user);
        }
    }
}
```
@Secret(BaseVO.class)定义在了UserController类上，表示整个类下面的方法都会实现AOP加密解密。BaseVO是所有vo的基类，其中只定义了一个字段encryptStr，也就是前端传递的加密参数字段。
> BaseVO.java
```java
package com.agger.springbootaopdemo.vo;

import lombok.Data;

/**
 * @program: BaseVO
 * @description: 基类
 * @author: chenhx
 * @create: 2019-12-05 15:15
 **/
@Data
public class BaseVO {
    // 加密密文
    private String encryptStr;
}
```
> UserVO.java
```java
package com.agger.springbootaopdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: User
 * @description: 用户
 * @author: chenhx
 * @create: 2019-12-03 15:31
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO extends BaseVO{
    private Integer id;
    private String name;
}
```
2. @Secret注解作用在方法上，表示只有此方法才需要执行AOP加密解密
> DeptController.java
```java
package com.agger.springbootaopdemo.controller;

import com.agger.springbootaopdemo.annotation.Secret;
import com.agger.springbootaopdemo.vo.DeptVO;
import com.agger.springbootaopdemo.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

/**
 * @program: Dept
 * @description: 部门类
 * @author: chenhx
 * @create: 2019-12-03 15:26
 **/
@RestController
@RequestMapping("dept")
public class DeptController {

    @GetMapping("getDeptName/{id}")
    public ResultVO getDeptName(@PathVariable("id") String id){
        return new ResultVO(0,"查询成功","财务部" + id);
    }

    // 注解在方法上，并传递了encryptStrName自己定义的加密字符串名称encryptJson
    @Secret(value = DeptVO.class,encryptStrName = "encryptJson")
    @PostMapping("addDept")
    public ResultVO addDept(@RequestBody DeptVO dept){
        return new ResultVO(0,"新增成功",dept);
    }
    
}
```
DeptVO类没有继承BaseVO类，自己写了一个前端需要传递的加密字符串字段，并传递给注解。ResultVO为接口响应类。

>DeptVO.java
```java
package com.agger.springbootaopdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: Dept
 * @description: 部门类
 * @author: chenhx
 * @create: 2019-12-03 15:32
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptVO{

    private Integer id;
    private String deptName;

    // 自己实现的一个参数，用来给前端传递加密字符串
    private String encryptJson;

}
```


>ResultVO.java
```java
package com.agger.springbootaopdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ResultVO
 * @description: 响应类
 * @author: chenhx
 * @create: 2019-12-03 15:34
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO {
    private Integer code;
    private String msg;
    private Object data;
}

```
#### 4. 定义AOP切面
万事具备，咱只欠定义一个AOP切面来实现加密和解密操作了。

> SecretAOPController.java
```java
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
```
1. 定义字段isSecret通过@Value从配置文件中注入true/false，来规定是否执行全局的AOP加密解密，在开发测试环境我们可以配置为不加密解密，方便查找错误。当项目上线运行时，可以配置true，实现加密操作。默认不配置为true
> application.yml
```yml
# 配置是否开启AOP参数加密解密，不配置默认为true
isSecret: true
```
2. 定义了AOP的切入点@Pointcut("@within(Secret)||@annotation(Secret)")，@Within表示匹配类上的指定注解，@annotation表示匹配方法上的指定注解
3. 使用环绕通知切面来实现加密和解密。point.proceed(args);表示执行请求方法，此方法之前表示前置切点，此方法之后表示后置切点。args是已经解密后的参数重新赋值传入。
4. 通过被代理的接口方法反射找到@Secret注解是在类上还是在方法上，并获取@Secret注解类对象，找到传入的value和encryptStrName
5. 我默认只有是单一参数的接口使用vo接收参数的惨进行加密解密，如果是单一参数比如 `getUser(Integer id) { ... }`这种形式的接口建议全部用vo来接收，并且继承BaseVO基类，具有encryptStr字段，或者你自定义的加密字段，在你使用@Secret的时候，别忘了填写
```java
// 注解在方法上，并传递了encryptStrName自己定义的加密字符串名称
@Secret(value = DeptVO.class,encryptStrName = "encryptJson")
```

#### 5. 执行效果
我们可以通过postman或其他前端工具来调取接口，在这里我使用的是idea自带的接口调试工具。
选择工具栏的Tools > HTTP Client > TestRESTfull Web Service
![](https://github.com/AggerChen/imageLibrary/blob/master/spring-boot-aop-demo/Image1.png)
点击后，就会在底部打开这个工具，跟postman一样，可以编辑请求方法，请求参数等等。
![](https://github.com/AggerChen/imageLibrary/blob/master/spring-boot-aop-demo/Image2.png)
不过，看点上面的提示没有？**This REST Client is deprecated.Try our new HTTP Client in the editor**
已经过时了，请使用新的客户端编辑，点击右边的按钮，idea就会给我们生成一个.http结尾的请求文件，我们可以像编辑配置文件一样，编辑请求，然后点击执行！！每个请求以三个#好分割，并可以分别执行。
![](https://github.com/AggerChen/imageLibrary/blob/master/spring-boot-aop-demo/Image3.png)

1. 编写好我的请求文件，执行addUser方法试试。
![](https://github.com/AggerChen/imageLibrary/blob/master/spring-boot-aop-demo/Image4.png)
执行成功！看到没，请求传递的加密参数，经过AOP解密传递给addUser方法，并返回给AOP加密给前端，并不是简单的直接传递噢，中间经历了解密再加密的过程。

2. 执行addDept方法测试一下，这个接口我们吧@Secret注解在了接口方法上，并传递了自定义的加密字段encryptJson。所以前端传递的加密参数名应该为encryptJson
![](https://github.com/AggerChen/imageLibrary/blob/master/spring-boot-aop-demo/Image5.png)

> AESUtils就是一个普通的AES加密工具，在此没有展示出来，需要的可以去我的 [github](https://github.com/AggerChen/spring-boot-aop-demo) 获取，欢迎star

### 三、总结

1. 此篇文章实现，使用了自定注解的功能，其实自定义注解无非就是这样用，通过反射来获取操作和标识作用。
2. AOP面向切面，其实SpringBoot的自动配置就是使用的AOP切面来实现的，我们通过自己实现一个切面，可以了解到整个执行流程和反射的应用。
3. 建议需要加密操作的接口参数都用vo对象来接收，因为切面中只能获取接口参数，不能获取到接口不存在的参数名。所以前端在传递加密参数名的时候，一定要保证接口参数具有相同的名称来接收，解密后再赋值其他参数。
4. AOP切面还有很多的实现，比如@Before @After @AfterReturning等，在此不过多讲解，本片只使用了@Around环绕通知。你也可以将解密加密分别拆分到@Before和@After中去执行。
5. rest-api.http文件是idea HTTPClient工具生成的api测试文件，默认没保存在项目中，我已经将此文件放在了test目录下，大家可以查看。
5. 本片文章参考了博客 [https://blog.csdn.net/lmb55/article/details/82470388](https://blog.csdn.net/lmb55/article/details/82470388)
6. 本篇文章的所有示例，都上传到我的github中，欢迎大家拉取测试，欢迎star [github](https://github.com/AggerChen/spring-boot-aop-demo)

