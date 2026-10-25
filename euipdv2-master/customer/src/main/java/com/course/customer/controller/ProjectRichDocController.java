/**
 * Author：周朔鹏
 * 编制时间：2026-10-05
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RichDocRequest;
import com.course.server.service.rdms.rich.CharacterRichDocService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 开发版本下全部特性的内容文档演进关系（模块 PROJECT）。
 */
@RestController
@RequestMapping("/project/rich-doc")
public class ProjectRichDocController {

    @Resource
    private CharacterRichDocService characterRichDocService;

    @PostMapping("/get-lineage")
    public ResponseDto<Map<String, Object>> getLineage(@RequestBody RichDocRequest req) {
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        dto.setContent(characterRichDocService.projectLineage(req.getPreProjectId(), req.getProjectId(),
                Boolean.TRUE.equals(req.getIncludeWork()), req.depthOrDefault()));
        return dto;
    }
}
