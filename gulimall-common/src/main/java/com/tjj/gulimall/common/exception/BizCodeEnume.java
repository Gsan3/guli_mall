package com.tjj.gulimall.common.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * 错误和错误信息定义类
 * 1、错误码规则的为5位数字
 * 2、前两位表示业务情景，最后三位表示错误码。例如100001。10通用，001系统未知异常
 * 3、维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10：通用
 *      001：参数校验失败
 *  11：商品
 *  12：订单
 *  13：购物车
 *  14：物流
 *
 */
@ControllerAdvice
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知错误"),
    VALUE_EXCEPTION(100001,"参数校验失败");

    private int code;
    private String msg;

    BizCodeEnume(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
