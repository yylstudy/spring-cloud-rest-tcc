package com.github.prontera;

import lombok.Value;

/**
 * 消息路由对象
 * @author Zhao Junjian
 */
@Value
public class MessageRoute {
    /**交换器名称*/
    private String exchange;
    /**路由键*/
    private String routeKey;
}
