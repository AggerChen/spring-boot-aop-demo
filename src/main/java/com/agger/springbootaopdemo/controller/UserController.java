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
