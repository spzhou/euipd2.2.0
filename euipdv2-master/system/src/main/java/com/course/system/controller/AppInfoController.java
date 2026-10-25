/**
 * Author：周朔鹏
 * 编制时间：2026-10-13
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.controller;

import com.course.server.dto.ResponseDto;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统版本：取 jar 内 META-INF/build-info.properties（构建时由 spring-boot-maven-plugin 生成）。
 * 前缀 /system/app-info/ 映射 MEMBER，机构内成员均可读。
 */
@RestController
@RequestMapping("/app-info")
public class AppInfoController {

    private static final String UNKNOWN = "unknown";

    private final ObjectProvider<BuildProperties> buildProperties;

    public AppInfoController(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/version")
    public ResponseDto<Map<String, String>> version() {
        ResponseDto<Map<String, String>> dto = new ResponseDto<>();
        Map<String, String> info = new LinkedHashMap<>();
        BuildProperties build = buildProperties.getIfAvailable();
        if (build == null) {
            String impl = AppInfoController.class.getPackage().getImplementationVersion();
            info.put("version", StringUtils.hasText(impl) ? impl : UNKNOWN);
            info.put("buildTime", null);
            info.put("commit", null);
        } else {
            info.put("version", build.getVersion());
            info.put("buildTime", build.getTime() == null ? null : build.getTime().toString());
            String commit = build.get("commit");
            info.put("commit", !StringUtils.hasText(commit) || UNKNOWN.equals(commit) ? null : commit);
        }
        dto.setContent(info);
        return dto;
    }
}
