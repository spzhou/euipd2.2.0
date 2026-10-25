/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.service.rdms.flow.FlowBizCode;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 启动时发布档案、质量、领料与退料的平台默认流程。全文未变则不再部署，不改机构定义。
 */
@Component
@Order(0)
public class Stage4PlatformDefinitionSeeder implements ApplicationRunner {

    @Resource
    private FlowDefinitionService flowDefinitionService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publish(FlowBizCode.FILE_APPLY.getCode(), "/flow/file-apply-default.bpmn20.xml");
        publish(FlowBizCode.QUALITY.getCode(), "/flow/quality-default.bpmn20.xml");
        publish(FlowBizCode.MATERIAL.getCode(), "/flow/material-default.bpmn20.xml");
        publish(FlowBizCode.MATERIAL_RETURN.getCode(), "/flow/material-return-default.bpmn20.xml");
    }

    private void publish(String bizCode, String resource) throws Exception {
        String xml;
        try (InputStream in = Stage4PlatformDefinitionSeeder.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("缺少平台默认流程定义 " + resource);
            }
            xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        flowDefinitionService.publishPlatform(bizCode, xml);
    }
}
