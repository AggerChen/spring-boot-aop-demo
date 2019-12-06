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

    // 注解在方法上，并传递了encryptStrName自己定义的加密字符串名称
    @Secret(value = DeptVO.class,encryptStrName = "encryptJson")
    @PostMapping("addDept")
    public ResultVO addDept(@RequestBody DeptVO dept){
        return new ResultVO(0,"新增成功",dept);
    }

}
