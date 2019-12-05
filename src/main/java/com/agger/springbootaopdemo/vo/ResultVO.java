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
