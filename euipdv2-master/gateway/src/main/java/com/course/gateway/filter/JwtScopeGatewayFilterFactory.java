/**
 * Author：周朔鹏
 * 编制时间：2026-08-01
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class JwtScopeGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Resource
    private JwtScopeGatewayFilter jwtScopeGatewayFilter;

    @Override
    public GatewayFilter apply(Object config) {
        return jwtScopeGatewayFilter;
    }
}
