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
